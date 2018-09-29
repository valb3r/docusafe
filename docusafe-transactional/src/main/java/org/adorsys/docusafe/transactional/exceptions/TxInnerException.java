package org.adorsys.docusafe.transactional.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

/**
 * Created by peter on 29.09.18 at 16:13.
 */
public class TxInnerException extends BaseException {
    public TxInnerException() {
        super("INNER TRANSACTION NOT ALLOWED YET");
    }
}
