package org.adorsys.docusafe.transactional.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.storeconnectionfactory.ReadArguments;
import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.service.impl.UserMetaDataUtil;
import org.adorsys.docusafe.transactional.exceptions.TxRacingConditionException;
import org.adorsys.docusafe.transactional.impl.helper.Class2JsonHelper;
import org.adorsys.docusafe.transactional.impl.helper.CleanupLogic;
import org.adorsys.docusafe.transactional.impl.helper.TransactionInformation;
import org.adorsys.docusafe.transactional.impl.helper.TransactionInformationList;
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
    private static DocumentFQN txidLogFilename = TransactionalDocumentSafeServiceImpl.txMeta.addName(LOG_FILE_NAME);
    public final static boolean dontEncrypt = System.getProperty(ReadArguments.NO_ENCRYPTION_PASSWORD) != null;


    private TransactionInformationList txidList = new TransactionInformationList();

    public static LastCommitedTxID findLastCommitedTxID(DocumentSafeService documentSafeService, UserIDAuth userIDAuth) {
        if (documentSafeService.documentExists(userIDAuth, txidLogFilename)) {
            DSDocument dsDocument = documentSafeService.readDocument(userIDAuth, txidLogFilename);
            TxIDLog txIDLog = new Class2JsonHelper().txidLogFromContent(dsDocument.getDocumentContent());
            if (txIDLog.txidList.isEmpty()) {
                throw new BaseException("file " + txidLogFilename + " must not be empty");
            }
            int size = txIDLog.txidList.size();
            if (size > MAX_COMMITED_TX_FOR_CLEANUP) {
                txIDLog.txidList = CleanupLogic.cleaupTxHistory(documentSafeService, userIDAuth, txIDLog.txidList);
                size = txIDLog.txidList.size();
                DSDocumentMetaInfo metaInfo = new DSDocumentMetaInfo();
                if (dontEncrypt) {
                    LOGGER.debug("save " + txidLogFilename + " unencrypted");
                    UserMetaDataUtil.setNoEncryption(metaInfo);
                }
                DSDocument document = new DSDocument(txidLogFilename, new Class2JsonHelper().txidLogToContent(txIDLog), metaInfo);
                documentSafeService.storeDocument(userIDAuth, document);
            }
            TransactionInformation lastTuple = txIDLog.txidList.get(size - 1);
            return new LastCommitedTxID(lastTuple.getCurrentTxID().getValue());
        }
        return null;
    }

    public static void saveJustFinishedTx(DocumentSafeService documentSafeService, UserIDAuth userIDAuth, Date start, Date finished, LastCommitedTxID previousTxID, TxID currentTxID) {
        // we synchonize not all methods, but those, refering to the same user
        synchronized(userIDAuth.getUserID().getValue()) {
            TxIDLog txIDLog = new TxIDLog();
            DSDocumentMetaInfo metaInfo = new DSDocumentMetaInfo();
            if (dontEncrypt) {
                LOGGER.debug("save " + txidLogFilename + " encrypted");
                UserMetaDataUtil.setNoEncryption(metaInfo);
            }
            if (documentSafeService.documentExists(userIDAuth, txidLogFilename)) {
                DSDocument dsDocument = documentSafeService.readDocument(userIDAuth, txidLogFilename);
                txIDLog = new Class2JsonHelper().txidLogFromContent(dsDocument.getDocumentContent());
                metaInfo = dsDocument.getDsDocumentMetaInfo();
            }
            if (!txIDLog.txidList.isEmpty()) {
                TransactionInformation lastTuple = txIDLog.txidList.get(txIDLog.txidList.size() - 1);
                LastCommitedTxID lastCommitedTxID = new LastCommitedTxID(lastTuple.getCurrentTxID().getValue());
                if (!lastCommitedTxID.equals(previousTxID)) {
                        throw new TxRacingConditionException(currentTxID, lastCommitedTxID, previousTxID);
                }
            }

            txIDLog.txidList.add(new TransactionInformation(start, finished, previousTxID, currentTxID));
            DSDocument document = new DSDocument(txidLogFilename, new Class2JsonHelper().txidLogToContent(txIDLog), metaInfo);
            documentSafeService.storeDocument(userIDAuth, document);
            LOGGER.debug("successfully wrote new Version to " + txidLogFilename);
        }
    }




}
