package org.adorsys.docusafe.transactional.impl;

import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.transactional.RequestMemoryContext;
import org.adorsys.docusafe.transactional.TransactionalDocumentSafeService;
import org.adorsys.docusafe.transactional.exceptions.TxInnerException;
import org.adorsys.docusafe.transactional.exceptions.TxNotActiveException;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.adorsys.encobject.types.PublicKeyJWK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by peter on 11.06.18 at 15:01.
 */
public class TransactionalDocumentSafeServiceImpl extends NonTransactionalDocumentSafeServiceImpl implements TransactionalDocumentSafeService {
    private final static Logger LOGGER = LoggerFactory.getLogger(TransactionalDocumentSafeServiceImpl.class);
    final static DocumentDirectoryFQN txMeta = new DocumentDirectoryFQN("meta.tx");
    final static DocumentDirectoryFQN txContent = new DocumentDirectoryFQN("tx");
    public static final String CURRENT_TRANSACTIONS_MAP = "CurrentTransactionData";

    public TransactionalDocumentSafeServiceImpl(RequestMemoryContext requestMemoryContext, DocumentSafeService documentSafeService) {
        super(requestMemoryContext, documentSafeService);
        LOGGER.debug("new Instance of TransactionalDocumentSafeServiceImpl");
    }

    // ============================================================================================
    // TRANSACTIONAL
    // ============================================================================================

    @Override
    public void beginTransaction(UserIDAuth userIDAuth) {
        Date beginTxDate = new Date();
        CurrentTransactionData currentTransactionData = (CurrentTransactionData) requestMemoryContext.get(CURRENT_TRANSACTIONS_MAP);
        if (currentTransactionData != null) {
            throw new TxInnerException();
        }

        TxID currentTxID = new TxID();
        LOGGER.debug("beginTransaction " + currentTxID.getValue());
        TxIDHashMap txIDHashMap = TxIDHashMap.fromPreviousFileOrNew(documentSafeService, userIDAuth, currentTxID, beginTxDate);
        currentTransactionData = new CurrentTransactionData(currentTxID, txIDHashMap);
        requestMemoryContext.put(CURRENT_TRANSACTIONS_MAP, currentTransactionData);
    }

    @Override
    public void txStoreDocument(UserIDAuth userIDAuth, DSDocument dsDocument) {
        LOGGER.debug("txStoreDocument " + dsDocument.getDocumentFQN().getValue() + " " + getCurrentTxID());
        documentSafeService.storeDocument(userIDAuth, modifyTxDocument(dsDocument, getCurrentTxID()));
        getCurrentTxIDHashMap().storeDocument(dsDocument.getDocumentFQN());
    }

    @Override
    public DSDocument txReadDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.debug("txReadDocument " + documentFQN.getValue() + " " + getCurrentTxID());
        TxID txidOfDocument = getCurrentTxIDHashMap().readDocument(documentFQN);
        DSDocument dsDocument = documentSafeService.readDocument(userIDAuth, modifyTxDocumentName(documentFQN, txidOfDocument));
        return new DSDocument(documentFQN, dsDocument.getDocumentContent(), dsDocument.getDsDocumentMetaInfo());
    }

    @Override
    public void txDeleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.debug("txDeleteDocument " + documentFQN.getValue() + " " + getCurrentTxID());
        getCurrentTxIDHashMap().deleteDocument(documentFQN);
    }

    @Override
    public BucketContentFQN txListDocuments(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        LOGGER.debug("txListDocuments " + getCurrentTxID());
        TxIDHashMap txIDHashMap = getCurrentTxIDHashMap();
        return txIDHashMap.list(documentDirectoryFQN, recursiveFlag);
    }

    @Override
    public boolean txDocumentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.debug("txDocumentExists " + documentFQN.getValue() + " " + getCurrentTxID());
        return getCurrentTxIDHashMap().documentExists(documentFQN);
    }

    @Override
    public void txDeleteFolder(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN) {
        LOGGER.debug("txDeleteFolder " + documentDirectoryFQN.getValue() + " " + getCurrentTxID());
        getCurrentTxIDHashMap().deleteFolder(documentDirectoryFQN);
    }

    @Override
    public void endTransaction(UserIDAuth userIDAuth) {
        TxID txid = getCurrentTxID();
        LOGGER.debug("endTransaction " + txid.getValue());
        boolean changed = getCurrentTransactionData().anyDifferenceToInitalState();
        if (changed) {
            LOGGER.info("something has changed, so write down the new state");
            TxIDHashMap txIDHashMap = getCurrentTxIDHashMap();
            txIDHashMap.setEndTransactionDate(new Date());
            txIDHashMap.saveOnce(documentSafeService, userIDAuth);
            txIDHashMap.transactionIsOver(documentSafeService, userIDAuth);
        } else {
            LOGGER.info("nothing has changed, so nothing has to be written down");
        }
        setCurrentTransactionMapToNull();
    }

    public static DSDocument modifyTxDocument(DSDocument dsDocument, TxID txid) {
        return new DSDocument(
                modifyTxDocumentName(dsDocument.getDocumentFQN(), txid),
                dsDocument.getDocumentContent(),
                dsDocument.getDsDocumentMetaInfo());
    }

    public static DocumentFQN modifyTxDocumentName(DocumentFQN origName, TxID txid) {
        return txContent.addName(origName.getValue() + "." + txid.getValue());
    }

    public static DocumentFQN modifyTxMetaDocumentName(DocumentFQN origName, TxID txid) {
        return txMeta.addName(origName.getValue() + "." + txid.getValue());
    }

    private TxID getCurrentTxID() {
        return getCurrentTransactionData().getCurrentTxID();
    }

    private TxIDHashMap getCurrentTxIDHashMap() {
        return getCurrentTransactionData().getCurrentTxHashMap();
    }

    private CurrentTransactionData getCurrentTransactionData() {
        CurrentTransactionData currentTransactionData = (CurrentTransactionData) requestMemoryContext.get(CURRENT_TRANSACTIONS_MAP);
        if (currentTransactionData == null) {
            throw new TxNotActiveException();
        }
        return currentTransactionData;
    }

    private void setCurrentTransactionMapToNull() {
        requestMemoryContext.put(CURRENT_TRANSACTIONS_MAP, null);
    }

    @Override
    public PublicKeyJWK findPublicEncryptionKey(UserID userID) {
        return documentSafeService.findPublicEncryptionKey(userID);
    }
}
