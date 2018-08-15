package org.adorsys.docusafe.cached.transactional.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.cached.transactional.CachedTransactionalDocumentSafeService;
import org.adorsys.docusafe.cached.transactional.exceptions.CacheException;
import org.adorsys.docusafe.transactional.RequestMemoryContext;
import org.adorsys.docusafe.transactional.TransactionalDocumentSafeService;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 21.06.18 at 11:51.
 * <p>
 * Es gibt drei Listen:
 * mapToStore enthält alle Dokumente, die gespeichert werden sollen.
 * Es wird nicht gepüft, ob sich der Inhalt gehändert hat, oder nicht.
 * <p>
 * mapToRead enthält alle Documente, die gelesen wurden. Wenn diese anschliessend
 * gespeichert werden, dann sind sie zusätzlich in mapToStore.
 * <p>
 * setToDelete enhält alle Namen der Dokumente, die gelöscht werden sollen.
 * Der name darf dann nicht in mapToRead oder mapToStore auftauchen.
 */
public class CachedTransactionalDocumentSafeServiceImpl implements CachedTransactionalDocumentSafeService {
    private final static Logger LOGGER = LoggerFactory.getLogger(CachedTransactionalDocumentSafeServiceImpl.class);
    public static final String CACHEND_TRANSACTIONAL_CONTEXT_MAP = "cachendTransactionalContextMap";
    private TransactionalDocumentSafeService transactionalFileStorage;
    private RequestMemoryContext requestContext;

    public CachedTransactionalDocumentSafeServiceImpl(RequestMemoryContext requestContext, TransactionalDocumentSafeService transactionalFileStorage) {
        this.transactionalFileStorage = transactionalFileStorage;
        this.requestContext = requestContext;
    }

    @Override
    public void createUser(UserIDAuth userIDAuth) {
        transactionalFileStorage.createUser(userIDAuth);
    }

    @Override
    public void destroyUser(UserIDAuth userIDAuth) {
        transactionalFileStorage.destroyUser(userIDAuth);
    }

    @Override
    public boolean userExists(UserID userID) {
        return transactionalFileStorage.userExists(userID);
    }

    @Override
    public void grantAccessToNonTxFolder(UserIDAuth userIDAuth, UserID receiverUserID, DocumentDirectoryFQN documentDirectoryFQN) {
        transactionalFileStorage.grantAccessToNonTxFolder(userIDAuth, receiverUserID, documentDirectoryFQN);
    }

    @Override
    public void nonTxStoreDocument(UserIDAuth userIDAuth, DSDocument dsDocument) {
        transactionalFileStorage.nonTxStoreDocument(userIDAuth, dsDocument);
    }

    @Override
    public void nonTxStoreDocument(UserIDAuth userIDAuth, UserID documentOwner, DSDocument dsDocument) {
        transactionalFileStorage.nonTxStoreDocument(userIDAuth, documentOwner, dsDocument);

    }

    @Override
    public DSDocument nonTxReadDocument(UserIDAuth userIDAuth, UserID documentOwner, DocumentFQN documentFQN) {
        return transactionalFileStorage.nonTxReadDocument(userIDAuth, documentOwner, documentFQN);
    }

    @Override
    public DSDocument nonTxReadDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        return transactionalFileStorage.nonTxReadDocument(userIDAuth, documentFQN);
    }

    @Override
    public boolean nonTxDocumentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        return transactionalFileStorage.nonTxDocumentExists(userIDAuth, documentFQN);
    }

    @Override
    public boolean nonTxDocumentExists(UserIDAuth userIDAuth, UserID documentOwner, DocumentFQN documentFQN) {
        return transactionalFileStorage.nonTxDocumentExists(userIDAuth, documentOwner, documentFQN);
    }

    @Override
    public void nonTxDeleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        transactionalFileStorage.nonTxDeleteDocument(userIDAuth, documentFQN);
    }

    @Override
    public BucketContentFQN nonTxListDocuments(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        return transactionalFileStorage.nonTxListDocuments(userIDAuth, documentDirectoryFQN, recursiveFlag);
    }

    @Override
    public TxID beginTransaction(UserIDAuth userIDAuth) {
        TxID txid = transactionalFileStorage.beginTransaction(userIDAuth);
        createTransactionalContext(txid);
        return txid;
    }

    @Override
    public void txStoreDocument(TxID txid, UserIDAuth userIDAuth, DSDocument dsDocument) {
        getTransactionalContext(txid).txStoreDocument(dsDocument);
    }

    @Override
    public DSDocument txReadDocument(TxID txid, UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        return getTransactionalContext(txid).txReadDocument(txid, userIDAuth, documentFQN);
    }

    @Override
    public void txDeleteDocument(TxID txid, UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        getTransactionalContext(txid).txDeleteDocument(documentFQN);
    }

    @Override
    public BucketContentFQN txListDocuments(TxID txid, UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        return getTransactionalContext(txid).txListDocuments(txid, userIDAuth, documentDirectoryFQN, recursiveFlag);
    }

    @Override
    public boolean txDocumentExists(TxID txid, UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        return getTransactionalContext(txid).txDocumentExists(txid, userIDAuth, documentFQN);
    }

    @Override
    public void txDeleteFolder(TxID txid, UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN) {
        throw new BaseException("Who needs this interface");

    }

    @Override
    public void endTransaction(TxID txid, UserIDAuth userIDAuth) {
        getTransactionalContext(txid).endTransaction(txid, userIDAuth);
        deleteTransactionalContext(txid);

    }

    private CachedTransactionalContext createTransactionalContext(TxID txid) {
        CachedTransactionalContextMap cachedTransactionalContextMap = (CachedTransactionalContextMap) requestContext.get(CACHEND_TRANSACTIONAL_CONTEXT_MAP);
        if (cachedTransactionalContextMap == null) {
            cachedTransactionalContextMap = new CachedTransactionalContextMap();
            requestContext.put(CACHEND_TRANSACTIONAL_CONTEXT_MAP, cachedTransactionalContextMap);
        }
        CachedTransactionalContext cachedTransactionalContext = new CachedTransactionalContext(transactionalFileStorage);
        cachedTransactionalContextMap.put(txid, cachedTransactionalContext);
        return cachedTransactionalContext;
    }

    private CachedTransactionalContext getTransactionalContext(TxID txid) {
        CachedTransactionalContextMap cachedTransactionalContextMap = (CachedTransactionalContextMap) requestContext.get(CACHEND_TRANSACTIONAL_CONTEXT_MAP);
        if (cachedTransactionalContextMap == null) {
            throw new CacheException("RequestContext has no CachedTransactionalContextMap. So Context for " + txid + " can not be searched");
        }
        CachedTransactionalContext cachedTransactionalContext = cachedTransactionalContextMap.get(txid);
        if (cachedTransactionalContext == null) {
            throw new CacheException("CachedTransactionalContextMap has no CachedContext for " + txid);
        }
        return cachedTransactionalContext;
    }

    private void deleteTransactionalContext(TxID txid) {
        CachedTransactionalContextMap cachedTransactionalContextMap = (CachedTransactionalContextMap) requestContext.get(CACHEND_TRANSACTIONAL_CONTEXT_MAP);
        if (cachedTransactionalContextMap == null) {
            throw new CacheException("RequestContext has no CachedTransactionalContextMap. So Context for " + txid + " can not be searched");
        }
        CachedTransactionalContext cachedTransactionalContext = cachedTransactionalContextMap.get(txid);
        if (cachedTransactionalContext == null) {
            throw new CacheException("CachedTransactionalContextMap has no CachedContext for " + txid);
        }
        cachedTransactionalContextMap.remove(txid);
    }
}
