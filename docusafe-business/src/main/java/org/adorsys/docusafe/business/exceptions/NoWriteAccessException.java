package org.adorsys.docusafe.business.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;

/**
 * Created by peter on 02.02.18 at 12:30.
 */
public class NoWriteAccessException extends BaseException {
    private UserID userID;
    private UserID owner;
    private DocumentFQN documentFQN;

    public NoWriteAccessException(UserID userID, UserID owner, DocumentFQN documentFQN) {
        super(userID.getValue() + " has not access right to write document in " + documentFQN.getValue() + " of " + owner.getValue());
        this.userID = userID;
        this.owner = owner;
        this.documentFQN = documentFQN;
    }

}
