package org.adorsys.docusafe.business.types.complex;

import org.adorsys.docusafe.business.types.UserID;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by peter on 29.01.18 at 18:04.
 *
 * War ursprünglich eine typisierte Map<UserID>
 * Das Json mapping von Maps ruft dann leider beim rausschreiben
 * den TypeAdapter nicht auf.
 * Auch ganz ohne TypeAdapter sieht eine in einer Map als Key
 * gespeicherte UserID nach json.to and from String nicht wieder
 * gleich aus. Die ganze Problematik wird umgangen durch Strings
 */

public class GrantAccessList extends HashSet<UserID> {

}
