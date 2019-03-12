package org.adorsys.docusafe.transactional.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.service.exceptions.NoDocumentGuardExists;
import org.adorsys.docusafe.service.impl.UserMetaDataUtil;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.docusafe.transactional.exceptions.NoTxFoundForDocumentException;
import org.adorsys.docusafe.transactional.exceptions.TxBaseException;
import org.adorsys.docusafe.transactional.types.TxBucketContentFQN;
import org.adorsys.docusafe.transactional.exceptions.TxAlreadyClosedException;
import org.adorsys.docusafe.transactional.exceptions.TxNotFoundException;
import org.adorsys.docusafe.transactional.impl.helper.BucketContentFromHashMapHelper;
import org.adorsys.docusafe.transactional.impl.helper.Class2JsonHelper;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by peter on 11.06.18 at 15:12.
 */

/**
 * Contains all documents, that exist for this transaction. the documents have been created from previous or this transaction.
 */
public class TxIDHashMap {
    private final static Logger LOGGER = LoggerFactory.getLogger(TxIDHashMap.class);
    private final static DocumentFQN filenamebase = new DocumentFQN("TransactionalHashMap.txt");

    private LastCommitedTxID lastCommitedTxID;
    private TxID currentTxID;
    private Date beginTx;
    private Date endTx;
    private HashMap<DocumentFQN, TxID> map = new HashMap<>();

    private TxIDHashMap(LastCommitedTxID lastCommitedTxID, TxID currentTx, Date beginTxDate) {
        this.lastCommitedTxID = lastCommitedTxID;
        this.currentTxID = currentTx;
        this.beginTx = beginTxDate;
        if (this.lastCommitedTxID == null) {
            this.lastCommitedTxID = new LastCommitedTxID("NULL");
        }
    }

    public HashMap<DocumentFQN, TxID> getMap() {
        return map;
    }

    public TxIDHashMap clone() {
        TxIDHashMap newTxIDHashMap = new TxIDHashMap(lastCommitedTxID, currentTxID, beginTx);
        newTxIDHashMap.map = new HashMap<>(map);
        return newTxIDHashMap;
    }

    public static TxIDHashMap fromPreviousFileOrNew(DocumentSafeService documentSafeService, UserIDAuth userIDAuth, TxID currentTxID, Date beginTxDate) {
        LastCommitedTxID lastKnownCommitedTxID = TxIDLog.findLastCommitedTxID(documentSafeService, userIDAuth);

        if (lastKnownCommitedTxID == null) {
            return new TxIDHashMap(lastKnownCommitedTxID, currentTxID, beginTxDate);
        }

        DocumentFQN file = TransactionalDocumentSafeServiceImpl.modifyTxMetaDocumentName(filenamebase, lastKnownCommitedTxID);
        TxIDHashMap map = readHashMapOfTx(documentSafeService, userIDAuth, lastKnownCommitedTxID);
        map.lastCommitedTxID = new LastCommitedTxID(map.currentTxID.getValue());
        map.currentTxID = currentTxID;
        map.beginTx = beginTxDate;
        map.endTx = null;
        return map;
    }

    public static TxIDHashMap readHashMapOfTx(DocumentSafeService documentSafeService, UserIDAuth userIDAuth, LastCommitedTxID lastCommitedTxID) {
        DocumentFQN file = TransactionalDocumentSafeServiceImpl.modifyTxMetaDocumentName(filenamebase, lastCommitedTxID);
        if (!documentSafeService.documentExists(userIDAuth, file)) {
            throw new TxNotFoundException(file, lastCommitedTxID);
        }
        DSDocument dsDocument = documentSafeService.readDocument(userIDAuth, file);
        return new Class2JsonHelper().txidHashMapFromContent(dsDocument.getDocumentContent());
    }

    public static void deleteHashMapOfTx(DocumentSafeService documentSafeService, UserIDAuth userIDAuth, LastCommitedTxID lastCommitedTxID) {
        DocumentFQN file = TransactionalDocumentSafeServiceImpl.modifyTxMetaDocumentName(filenamebase, lastCommitedTxID);
        if (!documentSafeService.documentExists(userIDAuth, file)) {
            throw new TxNotFoundException(file, lastCommitedTxID);
        }
        LOGGER.debug("delete transactional HashMap " + file);
        documentSafeService.deleteDocument(userIDAuth, file);
    }


    public void saveOnce(DocumentSafeService documentSafeService, UserIDAuth userIDAuth) {
        DocumentFQN file = TransactionalDocumentSafeServiceImpl.modifyTxMetaDocumentName(filenamebase, currentTxID);
        LOGGER.debug("save " + file.getValue());
        DocumentContent documentContent = new Class2JsonHelper().txidHashMapToContent(this);
        DSDocument dsDocument = new DSDocument(file, documentContent, new DSDocumentMetaInfo());
        if (TxIDLog.dontEncrypt) {
            LOGGER.debug("save " + file.getValue() + " encrypted");
            UserMetaDataUtil.setNoEncryption(dsDocument.getDsDocumentMetaInfo());
        }
        documentSafeService.storeDocument(userIDAuth, dsDocument);
    }

    public void storeDocument(DocumentFQN documentFQN) {
        map.put(documentFQN, currentTxID);
    }

    public TxID getTxIDOfDocument(DocumentFQN documentFQN) {
        TxID txID = map.get(documentFQN);
        if (txID == null) {
            throw new NoTxFoundForDocumentException(documentFQN);
        }
        return txID;
    }

    public void deleteDocument(DocumentFQN documentFQN) {
        map.remove(documentFQN);
    }

    public boolean documentExists(DocumentFQN documentFQN) {
        return map.containsKey(documentFQN);
    }

    public void deleteFolder(DocumentDirectoryFQN documentDirectoryFQN) {
        // Es müssen einfach alle Einträge gelöscht werden, die zu diesem directory gehören
        List<DocumentFQN> entriesToRemove = new ArrayList<>();
        map.keySet().forEach(documentFQN -> {
            if (documentFQN.getValue().startsWith(documentDirectoryFQN.getValue())) {
                entriesToRemove.add(documentFQN);
            }
        });
        entriesToRemove.forEach(documentFQN -> deleteDocument(documentFQN));
    }

    public void setEndTransactionDate(Date endTransactionDate) {
        endTx = endTransactionDate;
    }

    public void transactionIsOver(DocumentSafeService documentSafeService, UserIDAuth userIDAuth) {
        TxIDLog.saveJustFinishedTx(documentSafeService, userIDAuth, beginTx, endTx, lastCommitedTxID, currentTxID);
    }

    public TxBucketContentFQN list(DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        return BucketContentFromHashMapHelper.list(map, documentDirectoryFQN, recursiveFlag);
    }

    public void checkTxStillOpen() {
        if (endTx != null) {
            throw new TxAlreadyClosedException(currentTxID);
        }
    }
}
