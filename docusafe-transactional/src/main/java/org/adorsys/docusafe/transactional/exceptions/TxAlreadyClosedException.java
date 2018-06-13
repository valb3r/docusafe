package org.adorsys.docusafe.transactional.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.transactional.types.TxID;

/**
 * Created by peter on 13.06.18 at 18:01.
 */
public class TxAlreadyClosedException extends BaseException {
    public TxAlreadyClosedException(TxID txid) {
        super(txid.getValue());
    }
}
