package org.adorsys.docusafe.transactional.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.transactional.impl.helper.Class2JsonHelper;
import org.adorsys.docusafe.transactional.types.TxID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Created by peter on 11.06.18 at 15:48.
 */
public class TxIDLog {
    private final static int MAX_COMMITED_TX_FOR_CLEANUP = 5;
    private final static Logger LOGGER = LoggerFactory.getLogger(TxIDLog.class);
    private static String LOG_FILE_NAME = "LastCommitedTxID.txt";
    private static DocumentFQN txidLogFilename = TransactionalFileStorageImpl.txMeta.addName(LOG_FILE_NAME);

    private List<Tuple> txidList = new ArrayList<>();

    public static LastCommitedTxID findLastCommitedTxID(DocumentSafeService documentSafeService, UserIDAuth userIDAuth) {
        if (documentSafeService.documentExists(userIDAuth, txidLogFilename)) {
            DSDocument dsDocument = documentSafeService.readDocument(userIDAuth, txidLogFilename);
            TxIDLog txIDLog = new Class2JsonHelper().txidLogFromContent(dsDocument.getDocumentContent());
            if (txIDLog.txidList.isEmpty()) {
                throw new BaseException("file " + txidLogFilename + " must not be empty");
            }
            int size = txIDLog.txidList.size();
            if (size > MAX_COMMITED_TX_FOR_CLEANUP) {
                txIDLog = cleaup(documentSafeService, userIDAuth, txIDLog);
                size = txIDLog.txidList.size();
                DSDocumentMetaInfo metaInfo = new DSDocumentMetaInfo();
                metaInfo.setNoEncryption();
                DSDocument document = new DSDocument(txidLogFilename, new Class2JsonHelper().txidLogToContent(txIDLog), metaInfo);
                documentSafeService.storeDocument(userIDAuth, document);
            }
            Tuple lastTuple = txIDLog.txidList.get(size - 1);
            return new LastCommitedTxID(lastTuple.currentTxID.getValue());
        }
        return null;
    }

    public static void saveJustFinishedTx(DocumentSafeService documentSafeService, UserIDAuth userIDAuth, Date start, Date finished, LastCommitedTxID previousTxID, TxID currentTxID) {
        TxIDLog txIDLog = new TxIDLog();
        DSDocumentMetaInfo metaInfo = new DSDocumentMetaInfo();
        metaInfo.setNoEncryption();
        if (documentSafeService.documentExists(userIDAuth, txidLogFilename)) {
            DSDocument dsDocument = documentSafeService.readDocument(userIDAuth, txidLogFilename);
            txIDLog = new Class2JsonHelper().txidLogFromContent(dsDocument.getDocumentContent());
            metaInfo = dsDocument.getDsDocumentMetaInfo();
        }
        txIDLog.txidList.add(new Tuple(start, finished, previousTxID, currentTxID));
        DSDocument document = new DSDocument(txidLogFilename, new Class2JsonHelper().txidLogToContent(txIDLog), metaInfo);
        documentSafeService.storeDocument(userIDAuth, document);
        LOGGER.debug("successfully wrote new Version to " + txidLogFilename);
    }

    private final static class Tuple {
        private Date txDateFrom;
        private Date txDateUntil;
        private LastCommitedTxID previousTxID;
        private TxID currentTxID;

        public Tuple(Date txDateFrom, Date txDateUntil, LastCommitedTxID previousTxID, TxID currentTxID) {
            this.txDateFrom = txDateFrom;
            this.txDateUntil = txDateUntil;
            this.previousTxID = previousTxID;
            this.currentTxID = currentTxID;
        }
    }

    private static TxIDLog cleaup(DocumentSafeService documentSafeService, UserIDAuth userIDAuth, TxIDLog txIDLog) {
        int size = txIDLog.txidList.size();
        if (size < 1) {
            return txIDLog;
        }
        LOGGER.debug("cleanup has to be done for " + (size - 1) + " previously commited transactions");

        // Find all files exxcept the last tx
        HashSet<DocumentFQN> allPrevousFiles = new HashSet<>();
        {
            for (int i = 0; i < size - 1; i++) {
                Tuple tuple = txIDLog.txidList.get(i);
                TxIDHashMap txIDHashMap = TxIDHashMap.readHashMapOfTx(documentSafeService, userIDAuth, new LastCommitedTxID(tuple.currentTxID.getValue()));
                txIDHashMap.map.forEach((documentFQN, txID) -> allPrevousFiles.add(TransactionalFileStorageImpl.modifyTxDocumentName(documentFQN, txID)));
            }
        }

        // Find file of the last tx
        HashSet<DocumentFQN> currentFiles = new HashSet<>();
        {
            Tuple tuple = txIDLog.txidList.get(size - 1);
            TxIDHashMap txIDHashMap = TxIDHashMap.readHashMapOfTx(documentSafeService, userIDAuth, new LastCommitedTxID(tuple.currentTxID.getValue()));
            txIDHashMap.map.forEach((documentFQN, txID) -> currentFiles.add(TransactionalFileStorageImpl.modifyTxDocumentName(documentFQN, txID)));
        }
        LOGGER.debug("previous files size = " + allPrevousFiles.size());
        LOGGER.debug("current files size  = " + currentFiles.size());
        allPrevousFiles.removeAll(currentFiles);

        // delete all unused files
        LOGGER.debug("previous files size after removeing all of current file = " + allPrevousFiles.size());
        allPrevousFiles.forEach(fileToDelte -> documentSafeService.deleteDocument(userIDAuth, fileToDelte));

        // delete all metafiles of the previous tx
        {
            for (int i = 0; i < size - 1; i++) {
                Tuple tuple = txIDLog.txidList.get(i);
                TxIDHashMap.deleteHashMapOfTx(documentSafeService, userIDAuth, new LastCommitedTxID(tuple.currentTxID.getValue()));
            }
        }
        LOGGER.debug("expected HashMap to remain is " + txIDLog.txidList.get(size-1).currentTxID);


        // clear txLog and insert only the last tx
        {
            Tuple tuple = txIDLog.txidList.get(size - 1);
            TxIDLog newTxIdLog = new TxIDLog();
            newTxIdLog.txidList.add(tuple);
            return newTxIdLog;
        }
    }

}
