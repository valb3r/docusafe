package org.adorsys.docusafe.transactional.impl;

import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.transactional.RequestMemoryContext;
import org.adorsys.docusafe.transactional.TransactionalDocumentSafeService;
import org.adorsys.docusafe.transactional.exceptions.TxNotFoundException;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.types.ListRecursiveFlag;
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
    public TxID beginTransaction(UserIDAuth userIDAuth) {
        Date beginTxDate = new Date();
        TxID currentTxID = new TxID();
        LOGGER.debug("beginTransaction " + currentTxID.getValue());
        TxIDHashMap txIDHashMap = TxIDHashMap.fromPreviousFileOrNew(documentSafeService, userIDAuth, currentTxID, beginTxDate);
        CurrentTransactionsMap currentTransactionsMap = (CurrentTransactionsMap) requestMemoryContext.get(CURRENT_TRANSACTIONS_MAP);
        if (currentTransactionsMap == null) {
            currentTransactionsMap = new CurrentTransactionsMap();
            requestMemoryContext.put(CURRENT_TRANSACTIONS_MAP, currentTransactionsMap);
        }
        currentTransactionsMap.put(currentTxID, txIDHashMap);
        return currentTxID;
    }

    @Override
    public void txStoreDocument(TxID txid, UserIDAuth userIDAuth, DSDocument dsDocument) {
        LOGGER.debug("txStoreDocument " + dsDocument.getDocumentFQN().getValue() + " " + txid.getValue());
        TxIDHashMap txIDHashMap = getTxIDHashMap(txid);
        documentSafeService.storeDocument(userIDAuth, modifyTxDocument(dsDocument, txid));
        txIDHashMap.storeDocument(dsDocument.getDocumentFQN());
    }

    @Override
    public DSDocument txReadDocument(TxID txid, UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.debug("txReadDocument " + documentFQN.getValue() + " " + txid.getValue());
        TxIDHashMap txIDHashMap = getTxIDHashMap(txid);
        TxID txidOfDocument = txIDHashMap.readDocument(documentFQN);
        DSDocument dsDocument = documentSafeService.readDocument(userIDAuth, modifyTxDocumentName(documentFQN, txidOfDocument));
        return new DSDocument(documentFQN, dsDocument.getDocumentContent(), dsDocument.getDsDocumentMetaInfo());
    }

    @Override
    public void txDeleteDocument(TxID txid, UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.debug("txDeleteDocument " + documentFQN.getValue() + " " + txid.getValue());
        TxIDHashMap txIDHashMap = getTxIDHashMap(txid);
        txIDHashMap.deleteDocument(documentFQN);
    }

    @Override
    public BucketContentFQN txListDocuments(TxID txid, UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        TxIDHashMap txIDHashMap = getTxIDHashMap(txid);
        return txIDHashMap.list(documentDirectoryFQN, recursiveFlag);
    }

    @Override
    public boolean txDocumentExists(TxID txid, UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.debug("txDocumentExists " + documentFQN.getValue() + " " + txid.getValue());
        TxIDHashMap txIDHashMap = getTxIDHashMap(txid);
        return txIDHashMap.documentExists(documentFQN);
    }

    @Override
    public void txDeleteFolder(TxID txid, UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN) {
        LOGGER.debug("txDeleteFolder " + documentDirectoryFQN.getValue() + " " + txid.getValue());
        TxIDHashMap txIDHashMap = getTxIDHashMap(txid);
        txIDHashMap.deleteFolder(documentDirectoryFQN);
    }

    @Override
    public void endTransaction(TxID txid, UserIDAuth userIDAuth) {
        LOGGER.debug("transactionIsOver " + txid.getValue());
        TxIDHashMap txIDHashMap = getTxIDHashMap(txid);
        txIDHashMap.setEndTransactionDate(new Date());
        txIDHashMap.saveOnce(documentSafeService, userIDAuth);
        txIDHashMap.transactionIsOver(documentSafeService, userIDAuth);
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


    private TxIDHashMap getTxIDHashMap(TxID txid) {
        CurrentTransactionsMap currentTransactionsMap = (CurrentTransactionsMap) requestMemoryContext.get(CURRENT_TRANSACTIONS_MAP);
        if (currentTransactionsMap == null) {
            throw new TxNotFoundException(txid);
        }
        TxIDHashMap txidHashMap = currentTransactionsMap.get(txid);
        if (currentTransactionsMap == null) {
            throw new TxNotFoundException(txid);
        }
        txidHashMap.checkTxStillOpen();
        return txidHashMap;
    }



}
