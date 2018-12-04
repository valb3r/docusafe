package org.adorsys.docusafe.transactional.exceptions;

/**
 * Created by peter on 29.09.18 at 11:43.
 */
public class TxNotActiveException extends TxBaseException {
    public TxNotActiveException() {
        super("no transaction active");
    }
}
