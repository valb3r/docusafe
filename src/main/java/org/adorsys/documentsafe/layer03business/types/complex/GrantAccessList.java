package org.adorsys.documentsafe.layer03business.types.complex;

import org.adorsys.documentsafe.layer03business.types.AccessType;
import org.adorsys.documentsafe.layer03business.types.UserID;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by peter on 29.01.18 at 18:04.
 */
public class GrantAccessList {
    private Map<UserID, AccessType> map = new HashMap<>();

    public GrantAccessList() {
    }

    public void addOrReplace(UserID userID, AccessType accessType) {
        map.put(userID, accessType);
    }

}
