package org.adorsys.documentsafe.layer03business.exceptions;

import org.adorsys.documentsafe.layer00common.exceptions.BaseException;

/**
 * Created by peter on 19.01.18 at 15:35.
 */
public class UserIDAlreadyExistsException extends BaseException {
    public UserIDAlreadyExistsException(String message) {
        super(message);
    }
}
