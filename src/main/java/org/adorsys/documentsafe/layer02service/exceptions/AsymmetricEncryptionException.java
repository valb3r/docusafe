package org.adorsys.documentsafe.layer02service.exceptions;

import org.adorsys.documentsafe.layer00common.exceptions.BaseException;

/**
 * Created by peter on 10.01.18 at 09:00.
 */
public class AsymmetricEncryptionException extends BaseException {
    public AsymmetricEncryptionException(String message) {
        super(message);
    }
}
