package org.adorsys.docusafe.service.impl;

import org.adorsys.docusafe.service.types.DocumentKeyID;
import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKeyAndAccessType;
import org.adorsys.encobject.domain.KeyStoreAccess;
import org.adorsys.encobject.domain.ReadKeyPassword;

import java.util.HashMap;

/**
 * Created by peter on 26.06.18 at 19:40.
 */
public class GuardMap extends HashMap<String, PasswordAndDocumentKeyIDWithKeyAndAccessType> {

    public static String cacheKeyToString(KeyStoreAccess keyStoreAccess, DocumentKeyID documentKeyID) {
        return keyStoreAccess.getKeyStoreAuth().getReadStorePassword() + " " + keyStoreAccess.getKeyStorePath().toString() + " " + documentKeyID.toString();
    }

}


