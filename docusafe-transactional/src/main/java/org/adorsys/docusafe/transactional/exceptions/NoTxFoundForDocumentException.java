package org.adorsys.docusafe.transactional.exceptions;

import org.adorsys.docusafe.business.types.complex.DocumentFQN;

/**
 * Created by peter on 05.02.19 12:26.
 */
public class NoTxFoundForDocumentException extends TxBaseException {
    public NoTxFoundForDocumentException(DocumentFQN documentFQN) {
        super ("document is " + documentFQN);
    }
}
