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
import org.adorsys.docusafe.transactional.exceptions.TxNotActiveException;
import org.adorsys.docusafe.transactional.impl.CurrentTransactionData;
import org.adorsys.docusafe.transactional.impl.TransactionalDocumentSafeServiceImpl;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.adorsys.encobject.types.PublicKeyJWK;
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
    private RequestMemoryContext requestMemoryContext;

    public CachedTransactionalDocumentSafeServiceImpl(RequestMemoryContext requestMemoryContext, TransactionalDocumentSafeService transactionalFileStorage) {
        this.transactionalFileStorage = transactionalFileStorage;
        this.requestMemoryContext = requestMemoryContext;

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
    public PublicKeyJWK findPublicEncryptionKey(UserID userID) {
        return transactionalFileStorage.findPublicEncryptionKey(userID);
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
    public void nonTxDeleteFolder(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN) {
        transactionalFileStorage.nonTxDeleteFolder(userIDAuth, documentDirectoryFQN);
    }

    @Override
    public BucketContentFQN nonTxListDocuments(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        return transactionalFileStorage.nonTxListDocuments(userIDAuth, documentDirectoryFQN, recursiveFlag);
    }

    @Override
    public void beginTransaction(UserIDAuth userIDAuth) {
        transactionalFileStorage.beginTransaction(userIDAuth);
        createTransactionalContext(getCurrentTxID());
    }

    @Override
    public void txStoreDocument(UserIDAuth userIDAuth, DSDocument dsDocument) {
        getTransactionalContext(getCurrentTxID()).txStoreDocument(dsDocument);
    }

    @Override
    public DSDocument txReadDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        return getTransactionalContext(getCurrentTxID()).txReadDocument(userIDAuth, documentFQN);
    }

    @Override
    public void txDeleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        getTransactionalContext(getCurrentTxID()).txDeleteDocument(documentFQN);
    }

    @Override
    public BucketContentFQN txListDocuments(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        return getTransactionalContext(getCurrentTxID()).txListDocuments(userIDAuth, documentDirectoryFQN, recursiveFlag);
    }

    @Override
    public boolean txDocumentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        return getTransactionalContext(getCurrentTxID()).txDocumentExists(getCurrentTxID(), userIDAuth, documentFQN);
    }

    @Override
    public void txDeleteFolder(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN) {
        throw new BaseException("Who needs this interface");

    }

    @Override
    public void transferFromNonTxToTx(UserIDAuth userIDAuth, DocumentFQN nonTxFQN, DocumentFQN txFQN) {
        DSDocument nonTxDsDocument = nonTxReadDocument(userIDAuth, nonTxFQN);
        DSDocument txDsDocument = new DSDocument(txFQN, nonTxDsDocument.getDocumentContent(), nonTxDsDocument.getDsDocumentMetaInfo());
        txStoreDocument(userIDAuth, txDsDocument);
        getCurrentTransactionData().addNonTxFileToBeDeletedAfterCommit(nonTxFQN);
    }

    @Override
    public void endTransaction(UserIDAuth userIDAuth) {
        TxID txid = getCurrentTxID();
        getTransactionalContext(txid).endTransaction(userIDAuth);
        deleteTransactionalContext(txid);
    }

    private CachedTransactionalContext createTransactionalContext(TxID txid) {
        CachedTransactionalContextMap cachedTransactionalContextMap = (CachedTransactionalContextMap) requestMemoryContext.get(CACHEND_TRANSACTIONAL_CONTEXT_MAP);
        if (cachedTransactionalContextMap == null) {
            cachedTransactionalContextMap = new CachedTransactionalContextMap();
            requestMemoryContext.put(CACHEND_TRANSACTIONAL_CONTEXT_MAP, cachedTransactionalContextMap);
        }
        CachedTransactionalContext cachedTransactionalContext = new CachedTransactionalContext(transactionalFileStorage);
        cachedTransactionalContextMap.put(txid, cachedTransactionalContext);
        return cachedTransactionalContext;
    }

    private CachedTransactionalContext getTransactionalContext(TxID txid) {
        CachedTransactionalContextMap cachedTransactionalContextMap = (CachedTransactionalContextMap) requestMemoryContext.get(CACHEND_TRANSACTIONAL_CONTEXT_MAP);
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
        CachedTransactionalContextMap cachedTransactionalContextMap = (CachedTransactionalContextMap) requestMemoryContext.get(CACHEND_TRANSACTIONAL_CONTEXT_MAP);
        if (cachedTransactionalContextMap == null) {
            throw new CacheException("RequestContext has no CachedTransactionalContextMap. So Context for " + txid + " can not be searched");
        }
        CachedTransactionalContext cachedTransactionalContext = cachedTransactionalContextMap.get(txid);
        if (cachedTransactionalContext == null) {
            throw new CacheException("CachedTransactionalContextMap has no CachedContext for " + txid);
        }
        LOGGER.debug("freeMemory() of tx " + txid);

        cachedTransactionalContext.freeMemory();
        cachedTransactionalContextMap.remove(txid);
    }

    public TxID getCurrentTxID() {
        CurrentTransactionData currentTransactionData = getCurrentTransactionData();
        TxID txID = currentTransactionData.getCurrentTxID();
        if (txID == null) {
            throw new TxNotActiveException();
        }
        return txID;
    }

    private CurrentTransactionData getCurrentTransactionData() {
        CurrentTransactionData currentTransactionData = (CurrentTransactionData) requestMemoryContext.get(TransactionalDocumentSafeServiceImpl.CURRENT_TRANSACTION_DATA);
        if (currentTransactionData == null) {
            throw new TxNotActiveException();
        }
        return currentTransactionData;
    }
}
