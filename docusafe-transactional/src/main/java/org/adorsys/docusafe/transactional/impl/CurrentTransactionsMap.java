package org.adorsys.docusafe.transactional.impl;

import org.adorsys.docusafe.transactional.types.TxID;

import java.util.HashMap;

/**
 * Created by peter on 11.07.18 at 10:33.
 */

/**
 * Each user has its own context and thus its own transactional space.
 * This map contains ALL transactions for the user, using it.
 * ALL transactions are the open and the commited transactions.
 */
public class CurrentTransactionsMap extends HashMap<TxID, TxIDHashMap> {
    private TxID currentTxID = null;

    public TxID getCurrentTxID() {
        return currentTxID;
    }

    public void setCurrentTxID(TxID currentTxID) {
        this.currentTxID = currentTxID;
    }
}
