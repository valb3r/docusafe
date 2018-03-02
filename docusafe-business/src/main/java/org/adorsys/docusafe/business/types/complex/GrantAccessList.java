package org.adorsys.docusafe.business.types.complex;

import org.adorsys.docusafe.service.types.AccessType;
import org.adorsys.docusafe.business.types.UserID;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by peter on 29.01.18 at 18:04.
 *
 * War ursprünglich eine typisierte Map<UserID, AccessType>
 * Das Json mapping von Maps ruft dann leider beim rausschreiben
 * den TypeAdapter nicht auf.
 * Auch ganz ohne TypeAdapter sieht eine in einer Map als Key
 * gespeicherte UserID nach json.to and from String nicht wieder
 * gleich aus. Die ganze Problematik wird umgangen durch Strings
 */

public class GrantAccessList {
    private Map<String, AccessType> map = new HashMap<>();

    public GrantAccessList() {
    }

    public void addOrReplace(UserID userID, AccessType accessType) {
        if (AccessType.NONE.equals(accessType)) {
            map.remove(userID.getValue());
        } else {
            map.put(userID.getValue(), accessType);
        }
    }

    public AccessType find(UserID userID) {
        return map.get(userID.getValue());
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }
}
