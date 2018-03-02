package org.adorsys.docusafe.business.types.complex;

import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.encobject.domain.ReadKeyPassword;

/**
 * Created by peter on 19.01.18 at 14:49.
 */
public class UserIDAuth {
    private final UserID userID;
    private final ReadKeyPassword readKeyPassword;

    public UserIDAuth(UserID userID, ReadKeyPassword readKeyPassword) {
        this.userID = userID;
        this.readKeyPassword = readKeyPassword;
    }

    public ReadKeyPassword getReadKeyPassword() {
        return readKeyPassword;
    }

    public UserID getUserID() {

        return userID;
    }

    @Override
    public String toString() {
        return "UserIDAuth{" +
                userID + ", " + readKeyPassword +
                '}';
    }
}
