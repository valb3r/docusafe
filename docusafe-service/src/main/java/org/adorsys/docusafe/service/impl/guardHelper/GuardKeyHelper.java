package org.adorsys.docusafe.service.impl.guardHelper;

import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKeyAndAccessType;
import org.adorsys.encobject.domain.KeyStoreAccess;
import org.adorsys.encobject.service.api.KeystorePersistence;

/**
 * Created by peter on 16.02.18 at 17:46.
 */
public interface GuardKeyHelper {
    KeySourceAndGuardKeyID getKeySourceAndGuardKeyID(KeystorePersistence keystorePersistence,
                                                     KeyStoreAccess keyStoreAccess,
                                                     DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType);
}
