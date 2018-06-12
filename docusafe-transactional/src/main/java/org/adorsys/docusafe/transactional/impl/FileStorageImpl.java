package org.adorsys.docusafe.transactional.impl;

import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.transactional.FileStorage;
import org.adorsys.docusafe.transactional.impl.helper.TxIDVersionHelper;
import org.adorsys.docusafe.transactional.types.TxID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by peter on 11.06.18 at 15:01.
 */
public class FileStorageImpl implements FileStorage {
    private final static Logger LOGGER = LoggerFactory.getLogger(FileStorageImpl.class);
    final static DocumentDirectoryFQN txdir = new DocumentDirectoryFQN(FileStorage.class.getPackage().toString());

    private DocumentSafeService documentSafeService;

    public FileStorageImpl(DocumentSafeService documentSafeService) {
        LOGGER.debug("new Instance of FileStorageImpl");
        this.documentSafeService = documentSafeService;
    }

    @Override
    public TxID beginTransaction(UserIDAuth userIDAuth) {
        Date beginTxDate = new Date();
        TxID currentTxID = new TxID();
        LOGGER.debug("beginTransaction " + currentTxID.getValue());
        TxIDHashMap txIDHashMap = TxIDHashMap.fromPreviousFileOrNew(documentSafeService, userIDAuth, currentTxID, beginTxDate);
        txIDHashMap.save(documentSafeService, userIDAuth);
        return currentTxID;
    }

    @Override
    public void storeDocument(TxID txid, UserIDAuth userIDAuth, DSDocument dsDocument) {
        LOGGER.debug("storeDocument " + dsDocument.getDocumentFQN().getValue() + " " + txid.getValue());
        TxIDHashMap txIDHashMap = TxIDHashMap.getCurrentFile(documentSafeService, userIDAuth, txid);
        documentSafeService.storeDocument(userIDAuth, modify(dsDocument, txid));
        txIDHashMap.storeDocument(dsDocument.getDocumentFQN());
        txIDHashMap.save(documentSafeService, userIDAuth);
    }


    @Override
    public DSDocument readDocument(TxID txid, UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.debug("readDocument " + documentFQN.getValue() + " " + txid.getValue());
        TxIDHashMap txIDHashMap = TxIDHashMap.getCurrentFile(documentSafeService, userIDAuth, txid);
        TxID txidOfDocument = txIDHashMap.readDocument(documentFQN);
        return documentSafeService.readDocument(userIDAuth, TxIDVersionHelper.get(documentFQN, txidOfDocument));
    }

    @Override
    public void deleteDocument(TxID txid, UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.debug("deleteDocument " + documentFQN.getValue() + " " + txid.getValue());
        TxIDHashMap txIDHashMap = TxIDHashMap.getCurrentFile(documentSafeService, userIDAuth, txid);
        txIDHashMap.save(documentSafeService, userIDAuth);
        txIDHashMap.deleteDocument(documentFQN);
        txIDHashMap.save(documentSafeService, userIDAuth);
    }

    @Override
    public boolean documentExists(TxID txid, UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.debug("documentExists " + documentFQN.getValue() + " " + txid.getValue());
        TxIDHashMap txIDHashMap = TxIDHashMap.getCurrentFile(documentSafeService, userIDAuth, txid);
        return txIDHashMap.documentExists(documentFQN);
    }

    @Override
    public void deleteFolder(TxID txid, UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN) {
        LOGGER.debug("deleteFolder " + documentDirectoryFQN.getValue() + " " + txid.getValue());
        TxIDHashMap txIDHashMap = TxIDHashMap.getCurrentFile(documentSafeService, userIDAuth, txid);
        txIDHashMap.deleteFolder(documentDirectoryFQN);
        txIDHashMap.save(documentSafeService, userIDAuth);
    }

    @Override
    public void endTransaction(TxID txid, UserIDAuth userIDAuth) {
        LOGGER.debug("endTransaction " + txid.getValue());
        TxIDHashMap txIDHashMap = TxIDHashMap.getCurrentFile(documentSafeService, userIDAuth, txid);
        txIDHashMap.setEndTransactionDate(new Date());
        txIDHashMap.save(documentSafeService, userIDAuth);
    }


    private DSDocument modify(DSDocument dsDocument, TxID txid) {
        return new DSDocument(
                TxIDVersionHelper.get(dsDocument.getDocumentFQN(), txid),
                dsDocument.getDocumentContent(),
                dsDocument.getDsDocumentMetaInfo());
    }

}
