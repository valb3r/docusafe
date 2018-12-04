package org.adorsys.docusafe.transactional.exceptions;

import org.adorsys.docusafe.transactional.types.TxID;

/**
 * Created by peter on 03.12.18 14:06.
 */
public class TxRacingConditionException extends TxBaseException {
    public TxRacingConditionException(TxID lastCommited, TxID previous) {
        super(previous + " can not be committed, because the last commited tx is now " + lastCommited);
    }
}
