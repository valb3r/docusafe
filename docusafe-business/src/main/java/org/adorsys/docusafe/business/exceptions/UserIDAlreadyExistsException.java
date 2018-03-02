package org.adorsys.docusafe.business.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

/**
 * Created by peter on 19.01.18 at 15:35.
 */
public class UserIDAlreadyExistsException extends BaseException {
    public UserIDAlreadyExistsException(String message) {
        super(message);
    }
}
