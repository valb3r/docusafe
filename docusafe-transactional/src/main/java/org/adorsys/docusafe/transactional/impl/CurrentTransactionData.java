package org.adorsys.docusafe.transactional.impl;

import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.transactional.types.TxID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by peter on 11.07.18 at 10:33.
 */

/**
 * Each user has its own context and thus its own transactional space.
 * This map contains the current txmap of the user.
 */
public class CurrentTransactionData {
    private final static Logger LOGGER = LoggerFactory.getLogger(CurrentTransactionData.class);
    private TxID currentTxID = null;
    private TxIDHashMap currentTxHashMap = null;
    private TxIDHashMap initialTxHashMap = null;
    private Set<DocumentFQN> nonTxDocumentsToBeDeletedAfterCommit = new HashSet<>();

    public CurrentTransactionData(TxID currentTxID, TxIDHashMap currentTxHashMap) {
        this.currentTxID = currentTxID;
        this.currentTxHashMap = currentTxHashMap;
        initialTxHashMap = currentTxHashMap.clone();
    }

    public TxID getCurrentTxID() {
        return currentTxID;
    }

    public TxIDHashMap getCurrentTxHashMap() {
        return currentTxHashMap;
    }

    public boolean anyDifferenceToInitalState() {
        Set<DocumentFQN> currentFQNs = new HashSet<>(currentTxHashMap.map.keySet());
        Set<DocumentFQN> initialFQNs = new HashSet<>(initialTxHashMap.map.keySet());
        if (currentFQNs.size() > initialFQNs.size()) {
            currentFQNs.removeAll(initialFQNs);
            currentFQNs.forEach(fqn -> LOGGER.debug(" new file has been created: " + fqn));
            return true;
        }
        if (currentFQNs.size() < initialFQNs.size()) {
            initialFQNs.removeAll(currentFQNs);
            initialFQNs.forEach(fqn -> LOGGER.debug(" old file has been removed: " + fqn));
            return true;
        }
        for (DocumentFQN fqn : currentFQNs) {
            if (!initialFQNs.contains(fqn)) {
                LOGGER.debug(" old file has been removed: " + fqn);
                return true;
            }
            TxID currentTxID = currentTxHashMap.map.get(fqn);
            TxID initialTxID = initialTxHashMap.map.get(fqn);
            if (!currentTxID.equals(initialTxID)) {
                LOGGER.debug(" old file has changed:" + fqn);
                return true;
            }
        }
        LOGGER.debug(" nothing has changed");
        return false;
    }

    public void addNonTxFileToBeDeletedAfterCommit(DocumentFQN nonTxFQN) {
        nonTxDocumentsToBeDeletedAfterCommit.add(nonTxFQN);
    }

    public Set<DocumentFQN> getNonTxDocumentsToBeDeletedAfterCommit() {
        return nonTxDocumentsToBeDeletedAfterCommit;
    }
}
