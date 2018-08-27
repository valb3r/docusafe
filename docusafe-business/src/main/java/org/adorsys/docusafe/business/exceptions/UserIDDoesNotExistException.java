package org.adorsys.docusafe.business.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.types.UserID;

/**
 * Created by peter on 20.01.18 at 06:54.
 */
public class UserIDDoesNotExistException extends BaseException {
    public UserIDDoesNotExistException(UserID userID) {
        super(userID.getValue());
    }
}
