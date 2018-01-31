package org.adorsys.documentsafe.layer02service.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

/**
 * Created by peter on 10.01.18 at 08:49.
 */
public class SerializationException extends BaseException {
    public SerializationException(String message) {
        super(message);
    }
}
