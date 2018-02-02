package org.adorsys.documentsafe.layer03business.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

/**
 * Created by peter on 02.02.18 at 12:30.
 */
public class NoWriteAccessException extends BaseException {
    public NoWriteAccessException(String message) {
        super(message);
    }
}
