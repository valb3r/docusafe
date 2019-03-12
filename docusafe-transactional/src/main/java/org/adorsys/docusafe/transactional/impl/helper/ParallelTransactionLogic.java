package org.adorsys.docusafe.transactional.impl.helper;

import org.adorsys.docusafe.transactional.exceptions.TxRacingConditionException;
import org.adorsys.docusafe.transactional.impl.TxIDHashMap;
import org.adorsys.docusafe.transactional.impl.TxIDHashMapWrapper;
import org.adorsys.docusafe.transactional.types.TxID;

public class ParallelTransactionLogic {
    public TxIDHashMapWrapper join(TxIDHashMapWrapper stateLastCommittedTx, TxIDHashMapWrapper stateAtBeginOfCurrentTex, TxIDHashMapWrapper stateAtEndOfCurrentTx, TxIDHashMap documentsReadInTx) {
        throw new TxRacingConditionException(new TxID(), new TxID(), new TxID());
    }

}
