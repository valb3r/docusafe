package org.adorsys.docusafe.transactional.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;

/**
 * Created by peter on 13.06.18 at 19:35.
 */
public class TxGrantedDocumentMustNotContainFolderException extends BaseException {
    public TxGrantedDocumentMustNotContainFolderException(DocumentFQN documentFQN) {
        super(documentFQN.getValue());
    }
}
