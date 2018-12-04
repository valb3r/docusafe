package org.adorsys.docusafe.transactional.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

/**
 * Created by peter on 03.12.18 14:06.
 */
public class TxBaseException extends BaseException {
    public TxBaseException(String s) {
        super(s);
    }
}
