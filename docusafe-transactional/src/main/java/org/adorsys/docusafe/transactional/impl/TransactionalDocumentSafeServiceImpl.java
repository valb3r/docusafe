package org.adorsys.docusafe.transactional.impl;

import com.nimbusds.jose.jwk.JWK;
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
import org.adorsys.docusafe.transactional.exceptions.TxNotFoundException;
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
    public static final String CURRENT_TRANSACTIONS_MAP = "CurrentTransactionsMap";

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
        CurrentTransactionsMap currentTransactionsMap = (CurrentTransactionsMap) requestMemoryContext.get(CURRENT_TRANSACTIONS_MAP);
        if (currentTransactionsMap == null) {
            currentTransactionsMap = new CurrentTransactionsMap();
            requestMemoryContext.put(CURRENT_TRANSACTIONS_MAP, currentTransactionsMap);
        }
        if (currentTransactionsMap.getCurrentTxID() != null) {
            throw new TxInnerException();
        }
        TxID currentTxID = new TxID();
        LOGGER.debug("beginTransaction " + currentTxID.getValue());
        TxIDHashMap txIDHashMap = TxIDHashMap.fromPreviousFileOrNew(documentSafeService, userIDAuth, currentTxID, beginTxDate);
        currentTransactionsMap.put(currentTxID, txIDHashMap);
        currentTransactionsMap.setCurrentTxID(currentTxID);
    }

    @Override
    public void txStoreDocument(UserIDAuth userIDAuth, DSDocument dsDocument) {
        TxID txid = getCurrentTxID();
        LOGGER.debug("txStoreDocument " + dsDocument.getDocumentFQN().getValue() + " " + txid.getValue());
        TxIDHashMap txIDHashMap = getTxIDHashMap(txid);
        documentSafeService.storeDocument(userIDAuth, modifyTxDocument(dsDocument, txid));
        txIDHashMap.storeDocument(dsDocument.getDocumentFQN());
    }

    @Override
    public DSDocument txReadDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        TxID txid = getCurrentTxID();
        LOGGER.debug("txReadDocument " + documentFQN.getValue() + " " + txid.getValue());
        TxIDHashMap txIDHashMap = getTxIDHashMap(txid);
        TxID txidOfDocument = txIDHashMap.readDocument(documentFQN);
        DSDocument dsDocument = documentSafeService.readDocument(userIDAuth, modifyTxDocumentName(documentFQN, txidOfDocument));
        return new DSDocument(documentFQN, dsDocument.getDocumentContent(), dsDocument.getDsDocumentMetaInfo());
    }

    @Override
    public void txDeleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        TxID txid = getCurrentTxID();
        LOGGER.debug("txDeleteDocument " + documentFQN.getValue() + " " + txid.getValue());
        TxIDHashMap txIDHashMap = getTxIDHashMap(txid);
        txIDHashMap.deleteDocument(documentFQN);
    }

    @Override
    public BucketContentFQN txListDocuments(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        TxID txid = getCurrentTxID();
        TxIDHashMap txIDHashMap = getTxIDHashMap(txid);
        return txIDHashMap.list(documentDirectoryFQN, recursiveFlag);
    }

    @Override
    public boolean txDocumentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        TxID txid = getCurrentTxID();
        LOGGER.debug("txDocumentExists " + documentFQN.getValue() + " " + txid.getValue());
        TxIDHashMap txIDHashMap = getTxIDHashMap(txid);
        return txIDHashMap.documentExists(documentFQN);
    }

    @Override
    public void txDeleteFolder(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN) {
        TxID txid = getCurrentTxID();
        LOGGER.debug("txDeleteFolder " + documentDirectoryFQN.getValue() + " " + txid.getValue());
        TxIDHashMap txIDHashMap = getTxIDHashMap(txid);
        txIDHashMap.deleteFolder(documentDirectoryFQN);
    }

    @Override
    public void endTransaction(UserIDAuth userIDAuth) {
        TxID txid = getCurrentTxID();
        LOGGER.debug("transactionIsOver " + txid.getValue());
        TxIDHashMap txIDHashMap = getTxIDHashMap(txid);
        txIDHashMap.setEndTransactionDate(new Date());
        txIDHashMap.saveOnce(documentSafeService, userIDAuth);
        txIDHashMap.transactionIsOver(documentSafeService, userIDAuth);
        getCurrentTransactionMap().setCurrentTxID(null);
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
        CurrentTransactionsMap currentTransactionsMap = getCurrentTransactionMap();
        TxID txID = currentTransactionsMap.getCurrentTxID();
        if (txID == null) {
            throw new TxNotActiveException();
        }
        return txID;
    }


    private TxIDHashMap getTxIDHashMap(TxID txid) {
        CurrentTransactionsMap currentTransactionsMap = getCurrentTransactionMap();
        TxIDHashMap txidHashMap = currentTransactionsMap.get(txid);
        if (currentTransactionsMap == null) {
            throw new TxNotFoundException(txid);
        }
        txidHashMap.checkTxStillOpen();
        return txidHashMap;
    }

    private CurrentTransactionsMap getCurrentTransactionMap() {
        CurrentTransactionsMap currentTransactionsMap = (CurrentTransactionsMap) requestMemoryContext.get(CURRENT_TRANSACTIONS_MAP);
        if (currentTransactionsMap == null) {
            throw new TxNotActiveException();
        }
        return currentTransactionsMap;
    }

    @Override
    public PublicKeyJWK findPublicEncryptionKey(UserID userID) {
        return documentSafeService.findPublicEncryptionKey(userID);
    }
}
