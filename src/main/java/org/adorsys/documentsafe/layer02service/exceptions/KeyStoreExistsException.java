package org.adorsys.documentsafe.layer02service.exceptions;

import org.adorsys.documentsafe.layer00common.exceptions.BaseException;

/**
 * Created by peter on 20.01.18 at 17:09.
 */
public class KeyStoreExistsException extends BaseException {
    public KeyStoreExistsException(String message) {
        super(message);
    }
}
