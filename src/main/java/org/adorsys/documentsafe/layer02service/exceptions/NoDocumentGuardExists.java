package org.adorsys.documentsafe.layer02service.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

/**
 * Created by peter on 03.02.18 at 09:52.
 */
public class NoDocumentGuardExists extends BaseException {
    public NoDocumentGuardExists(String message) {
        super(message);
    }
}
