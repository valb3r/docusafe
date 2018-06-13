package org.adorsys.docusafe.transactional.impl;

import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.docusafe.transactional.exceptions.TxAlreadyClosedException;
import org.adorsys.docusafe.transactional.impl.helper.Class2JsonHelper;
import org.adorsys.docusafe.transactional.impl.helper.TxIDVersionHelper;
import org.adorsys.docusafe.transactional.types.TxID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by peter on 11.06.18 at 15:12.
 */
public class TxIDHashMap {
    private final static Logger LOGGER = LoggerFactory.getLogger(TxIDHashMap.class);
    private final static String HASHMAP_BASE_FILE_NAME = "TransactionalHashMap.txt";
    private final static DocumentFQN filenamebase = TransactionalFileStorageImpl.txdir.addName(HASHMAP_BASE_FILE_NAME);

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

    public static TxIDHashMap fromPreviousFileOrNew(DocumentSafeService documentSafeService, UserIDAuth userIDAuth, TxID currentTxID, Date beginTxDate) {
        LastCommitedTxID lastKnownCommitedTxID = TxIDLog.findLastCommitedTxID(documentSafeService, userIDAuth);

        if (lastKnownCommitedTxID == null) {
            return new TxIDHashMap(lastKnownCommitedTxID, currentTxID, beginTxDate);
        }

        DocumentFQN file = TxIDVersionHelper.get(filenamebase, lastKnownCommitedTxID);
        if (documentSafeService.documentExists(userIDAuth, file)) {
            DSDocument dsDocument = documentSafeService.readDocument(userIDAuth, file);
            TxIDHashMap map = new Class2JsonHelper().txidHashMapFromContent(dsDocument.getDocumentContent());
            map.lastCommitedTxID = new LastCommitedTxID(map.currentTxID.getValue());
            map.currentTxID = currentTxID;
            map.beginTx = beginTxDate;
            map.endTx = null;
            return map;
        }

        throw new RuntimeException("Can not find a HashMap " + file.getValue() + " though last commitedTxID seems to be " + lastKnownCommitedTxID);
    }

    public static TxIDHashMap getCurrentFile(DocumentSafeService documentSafeService, UserIDAuth userIDAuth, TxID currentTxID) {
        DocumentFQN file = TxIDVersionHelper.get(filenamebase, currentTxID);
        DSDocument dsDocument = documentSafeService.readDocument(userIDAuth, file);
        TxIDHashMap txIDHashMap = new Class2JsonHelper().txidHashMapFromContent(dsDocument.getDocumentContent());
        if (txIDHashMap.endTx != null) {
            throw new TxAlreadyClosedException(currentTxID);
        }
        return txIDHashMap;
    }

    public void save(DocumentSafeService documentSafeService, UserIDAuth userIDAuth) {
        DocumentFQN file = TxIDVersionHelper.get(filenamebase, currentTxID);
        LOGGER.debug("save " + file.getValue());
        DocumentContent documentContent = new Class2JsonHelper().txidHashMapToContent(this);
        DSDocument dsDocument = new DSDocument(file, documentContent, new DSDocumentMetaInfo());
        documentSafeService.storeDocument(userIDAuth, dsDocument);
    }

    public void storeDocument(DocumentFQN documentFQN) {
        map.put(documentFQN, currentTxID);
    }

    public TxID readDocument(DocumentFQN documentFQN) {
        return map.get(documentFQN);
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
}
