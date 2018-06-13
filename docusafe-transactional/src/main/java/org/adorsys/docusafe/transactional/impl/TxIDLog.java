package org.adorsys.docusafe.transactional.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.transactional.impl.helper.Class2JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by peter on 11.06.18 at 15:48.
 */
public class TxIDLog {
    private final static Logger LOGGER = LoggerFactory.getLogger(TxIDLog.class);
    private static String LOG_FILE_NAME = "LastCommitedTxID.txt";
    private static DocumentFQN txidLogFilename = TransactionalFileStorageImpl.txdir.addName(LOG_FILE_NAME);

    public static LastCommitedTxID findLastCommitedTxID(DocumentSafeService documentSafeService, UserIDAuth userIDAuth) {
        if (documentSafeService.documentExists(userIDAuth, txidLogFilename)) {
            DSDocument dsDocument = documentSafeService.readDocument(userIDAuth, txidLogFilename);
            TxIDLog txIDLog = new Class2JsonHelper().txidLogFromContent(dsDocument.getDocumentContent());
            if (txIDLog.txidList.isEmpty()) {
                throw new BaseException("file " + txidLogFilename + " must not be empty");
            }
            int size = txIDLog.txidList.size();
            if (size > 5) {
                for (int i = 0; i<10; i++) {
                    LOGGER.info("zeit im logfile aufzuräumen:" + txidLogFilename + " hat bereits " + size + " Einträge");
                }
            }
            Tripple lastTripple = txIDLog.txidList.get(size - 1);
            return lastTripple.txid;
        }
        return null;
    }

    public static void saveJustFinishedTx(DocumentSafeService documentSafeService, UserIDAuth userIDAuth, Date start, Date finished, LastCommitedTxID lastCommitedTxID) {
        TxIDLog txIDLog = new TxIDLog();
        DSDocumentMetaInfo metaInfo = new DSDocumentMetaInfo();
        if (documentSafeService.documentExists(userIDAuth, txidLogFilename)) {
            DSDocument dsDocument = documentSafeService.readDocument(userIDAuth, txidLogFilename);
            txIDLog = new Class2JsonHelper().txidLogFromContent(dsDocument.getDocumentContent());
            metaInfo = dsDocument.getDsDocumentMetaInfo();
        }
        txIDLog.txidList.add(new Tripple(start, finished, lastCommitedTxID));
        DSDocument document = new DSDocument(txidLogFilename, new Class2JsonHelper().txidLogToContent(txIDLog), metaInfo);
        documentSafeService.storeDocument(userIDAuth, document);
        LOGGER.info("successfully wrote new Version to " + txidLogFilename);
    }

    private List<Tripple> txidList = new ArrayList<>();

    private final static class Tripple {
        private Date txDateFrom;
        private Date txDateUntil;
        private LastCommitedTxID txid;

        public Tripple(Date txDateFrom, Date txDateUntil, LastCommitedTxID txid) {
            this.txDateFrom = txDateFrom;
            this.txDateUntil = txDateUntil;
            this.txid = txid;
        }
    }
}
