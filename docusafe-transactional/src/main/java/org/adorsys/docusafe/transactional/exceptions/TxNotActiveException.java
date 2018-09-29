package org.adorsys.docusafe.transactional.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

/**
 * Created by peter on 29.09.18 at 11:43.
 */
public class TxNotActiveException extends BaseException {
    public TxNotActiveException() {
        super("no transaction active");
    }
}
