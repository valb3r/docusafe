package org.adorsys.docusafe.business.impl;

import org.adorsys.docusafe.business.impl.caches.DocumentGuardCache;
import org.adorsys.docusafe.business.impl.caches.DocumentKeyIDCache;
import org.adorsys.docusafe.business.impl.caches.UserAuthCache;

/**
 * Created by peter on 10.08.18 at 13:54.
 */
public interface DocusafeCacheWrapper {
    UserAuthCache getUserAuthCache();
    DocumentGuardCache getDocumentGuardCache();
    DocumentKeyIDCache getDocumentKeyIDCache();
}
