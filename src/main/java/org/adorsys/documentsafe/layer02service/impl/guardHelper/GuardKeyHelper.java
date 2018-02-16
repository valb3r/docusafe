package org.adorsys.documentsafe.layer02service.impl.guardHelper;

import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKeyAndAccessType;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.encobject.service.KeystorePersistence;

/**
 * Created by peter on 16.02.18 at 17:46.
 */
public interface GuardKeyHelper {
    KeySourceAndGuardKeyID getKeySourceAndGuardKeyID(KeystorePersistence keystorePersistence,
                                                     KeyStoreAccess keyStoreAccess,
                                                     DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType);
}
