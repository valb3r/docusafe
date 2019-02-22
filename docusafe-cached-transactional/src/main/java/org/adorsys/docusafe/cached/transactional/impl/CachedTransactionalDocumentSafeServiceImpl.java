package org.adorsys.docusafe.cached.transactional.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.types.MoveType;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.BucketContentFQNWithUserMetaData;
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
import org.adorsys.docusafe.transactional.types.TxBucketContentFQN;
import org.adorsys.docusafe.transactional.types.TxDocumentFQNVersion;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.filesystem.exceptions.FileNotFoundException;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.adorsys.encobject.types.OverwriteFlag;
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
    public PublicKeyJWK findPublicEncryptionKey(UserID userID) {
        return transactionalFileStorage.findPublicEncryptionKey(userID);
    }

    @Override
    public BucketContentFQNWithUserMetaData nonTxListInbox(UserIDAuth userIDAuth) {
        return transactionalFileStorage.nonTxListInbox(userIDAuth);
    }

    @Override
    public void beginTransaction(UserIDAuth userIDAuth) {
        transactionalFileStorage.beginTransaction(userIDAuth);
        createTransactionalContext(getCurrentTxID(userIDAuth.getUserID()));
    }

    @Override
    public void txStoreDocument(UserIDAuth userIDAuth, DSDocument dsDocument) {
        getTransactionalContext(getCurrentTxID(userIDAuth.getUserID())).txStoreDocument(dsDocument);
    }

    @Override
    public DSDocument txReadDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        return getTransactionalContext(getCurrentTxID(userIDAuth.getUserID())).txReadDocument(userIDAuth, documentFQN);
    }

    @Override
    public void txDeleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        getTransactionalContext(getCurrentTxID(userIDAuth.getUserID())).txDeleteDocument(documentFQN);
    }

    @Override
    public TxBucketContentFQN txListDocuments(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        return getTransactionalContext(getCurrentTxID(userIDAuth.getUserID())).txListDocuments(userIDAuth, documentDirectoryFQN, recursiveFlag);
    }

    @Override
    public TxDocumentFQNVersion getVersion(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        TxBucketContentFQN txBucketContentFQN = txListDocuments(userIDAuth, documentFQN.getDocumentDirectory(), ListRecursiveFlag.FALSE);
        if (txBucketContentFQN.getFilesWithVersion().isEmpty()) {
            throw new FileNotFoundException(documentFQN.getValue(), null);
        }
        return txBucketContentFQN.getFilesWithVersion().stream().findFirst().get().getVersion();
    }

    @Override
    public boolean txDocumentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        return getTransactionalContext(getCurrentTxID(userIDAuth.getUserID())).txDocumentExists(userIDAuth, documentFQN);
    }

    @Override
    public void txDeleteFolder(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN) {
        TxBucketContentFQN txBucketContentFQN = getTransactionalContext(getCurrentTxID(userIDAuth.getUserID())).txListDocuments(userIDAuth, documentDirectoryFQN, ListRecursiveFlag.TRUE);
        txBucketContentFQN.getFiles().stream().forEach(documentFQN -> txDeleteDocument(userIDAuth, documentFQN));
    }

    @Override
    public void endTransaction(UserIDAuth userIDAuth) {
        TxID txid = getCurrentTxID(userIDAuth.getUserID());
        getTransactionalContext(txid).endTransaction(userIDAuth);
        deleteTransactionalContext(txid);
    }

    @Override
    public void txMoveDocumnetToInboxOfUser(UserIDAuth userIDAuth, UserID receiverUserID, DocumentFQN sourceDocumentFQN, DocumentFQN destDocumentFQN, MoveType moveType) {
        // da diese Methoden intern Methoden wie txRead und txStore aufrufen, findet so das caching statt
        transactionalFileStorage.txMoveDocumnetToInboxOfUser(userIDAuth, receiverUserID, sourceDocumentFQN, destDocumentFQN, moveType);
    }

    @Override
    public DSDocument nonTxReadFromInbox(UserIDAuth userIDAuth, DocumentFQN source, DocumentFQN destination, OverwriteFlag overwriteFlag) {
        // da diese Methoden intern Methoden wie txRead und txStore aufrufen, findet so das caching statt
        return transactionalFileStorage.nonTxReadFromInbox(userIDAuth, source, destination, overwriteFlag);
    }

    private CachedTransactionalContext createTransactionalContext(TxID txid) {
        CachedTransactionalContextMap cachedTransactionalContextMap = (CachedTransactionalContextMap) requestMemoryContext.get(CACHEND_TRANSACTIONAL_CONTEXT_MAP);
        if (cachedTransactionalContextMap == null) {
            cachedTransactionalContextMap = new CachedTransactionalContextMap();
            requestMemoryContext.put(CACHEND_TRANSACTIONAL_CONTEXT_MAP, cachedTransactionalContextMap);
        }
        CachedTransactionalContext cachedTransactionalContext = new CachedTransactionalContext(transactionalFileStorage, txid);
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

    public TxID getCurrentTxID(UserID userID) {
        CurrentTransactionData currentTransactionData = getCurrentTransactionData(userID);
        TxID txID = currentTransactionData.getCurrentTxID();
        if (txID == null) {
            throw new TxNotActiveException(userID);
        }
        return txID;
    }

    private CurrentTransactionData getCurrentTransactionData(UserID userID) {
        CurrentTransactionData currentTransactionData = (CurrentTransactionData) requestMemoryContext.get(TransactionalDocumentSafeServiceImpl.CURRENT_TRANSACTION_DATA + "-" + userID.getValue());
        if (currentTransactionData == null) {
            throw new TxNotActiveException(userID);
        }
        return currentTransactionData;
    }
}
