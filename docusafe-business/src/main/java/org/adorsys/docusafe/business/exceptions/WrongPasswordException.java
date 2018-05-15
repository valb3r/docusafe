package org.adorsys.docusafe.business.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.types.UserID;

/**
 * Created by peter on 15.05.18 at 17:35.
 */
public class WrongPasswordException extends BaseException {
    public WrongPasswordException(UserID userID) {
        super("Wrong Password for " + userID);
    }
}
