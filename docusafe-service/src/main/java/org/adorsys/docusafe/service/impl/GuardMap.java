package org.adorsys.docusafe.service.impl;

import org.adorsys.docusafe.service.types.DocumentKeyID;
import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKeyAndAccessType;
import org.adorsys.encobject.domain.KeyStoreAccess;

import java.util.HashMap;

/**
 * Created by peter on 26.06.18 at 19:40.
 */
public class GuardMap extends HashMap<String, DocumentKeyIDWithKeyAndAccessType> {

    public static String cacheKeyToString(KeyStoreAccess keyStoreAccess, DocumentKeyID documentKeyID) {
        return keyStoreAccess.getKeyStoreAuth().getReadKeyPassword().getValue() +
                keyStoreAccess.getKeyStoreAuth().getReadStorePassword().getValue() +
                keyStoreAccess.getKeyStorePath().toString() +
                documentKeyID.toString();
    }
}


