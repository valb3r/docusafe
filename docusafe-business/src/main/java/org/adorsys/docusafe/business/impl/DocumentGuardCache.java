package org.adorsys.docusafe.business.impl;

import org.adorsys.docusafe.service.impl.PasswordAndDocumentKeyIDWithKeyAndAccessType;
import org.adorsys.docusafe.service.types.DocumentKeyID;
import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKeyAndAccessType;
import org.adorsys.encobject.domain.KeyStoreAccess;
import org.adorsys.encobject.domain.ReadKeyPassword;

import java.util.HashMap;

/**
 * Created by peter on 26.06.18 at 19:40.
 */
public interface DocumentGuardCache {
    PasswordAndDocumentKeyIDWithKeyAndAccessType get(String key);
    void put(String key, PasswordAndDocumentKeyIDWithKeyAndAccessType value);
    void remove(String key);
    boolean containsKey(String key);

    static String cacheKeyToString(KeyStoreAccess keyStoreAccess, DocumentKeyID documentKeyID) {
            return keyStoreAccess.getKeyStoreAuth().getReadStorePassword() + " " + keyStoreAccess.getKeyStorePath().toString() + " " + documentKeyID.toString();
        }

}


