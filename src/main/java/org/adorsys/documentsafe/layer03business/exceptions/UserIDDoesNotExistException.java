package org.adorsys.documentsafe.layer03business.exceptions;

import org.adorsys.documentsafe.layer00common.exceptions.BaseException;

/**
 * Created by peter on 20.01.18 at 06:54.
 */
public class UserIDDoesNotExistException extends BaseException {
    public UserIDDoesNotExistException(String message) {
        super(message);
    }
}
