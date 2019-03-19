package org.adorsys.docusafe.business.impl.caches;

import org.adorsys.docusafe.service.impl.PasswordAndDocumentKeyIDWithKey;
import org.adorsys.docusafe.service.types.DocumentKeyID;
import org.adorsys.encobject.domain.KeyStoreAccess;

/**
 * Created by peter on 26.06.18 at 19:40.
 */
public interface DocumentGuardCache extends DocusafeCacheTemplate<String, PasswordAndDocumentKeyIDWithKey> {

    static String cacheKeyToString(KeyStoreAccess keyStoreAccess, DocumentKeyID documentKeyID) {
            return keyStoreAccess.getKeyStoreAuth().getReadStorePassword() + " " + keyStoreAccess.getKeyStorePath().toString() + " " + documentKeyID.toString();
        }

}


