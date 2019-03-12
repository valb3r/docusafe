package org.adorsys.docusafe.transactional.impl.helper;

import org.adorsys.docusafe.transactional.impl.LastCommitedTxID;
import org.adorsys.docusafe.transactional.types.TxID;

import java.util.Date;

public class TransactionInformation {
    private Date txDateFrom;
    private Date txDateUntil;
    private LastCommitedTxID previousTxID;
    private TxID currentTxID;

    public TransactionInformation(Date txDateFrom, Date txDateUntil, LastCommitedTxID previousTxID, TxID currentTxID) {
        this.txDateFrom = txDateFrom;
        this.txDateUntil = txDateUntil;
        this.previousTxID = previousTxID;
        this.currentTxID = currentTxID;
    }

    public Date getTxDateFrom() {
        return txDateFrom;
    }

    public Date getTxDateUntil() {
        return txDateUntil;
    }

    public LastCommitedTxID getPreviousTxID() {
        return previousTxID;
    }

    public TxID getCurrentTxID() {
        return currentTxID;
    }
}
