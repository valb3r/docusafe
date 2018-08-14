package org.adorsys.docusafe.business.impl.caches.guava;

import org.adorsys.docusafe.business.impl.caches.UserAuthCache;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.encobject.domain.ReadKeyPassword;

/**
 * Created by peter on 14.08.18 at 17:27.
 */
public class UserAuthCacheGuavaImpl
        extends DocusafeCacheTemplateGuavaImpl <UserID, ReadKeyPassword>
        implements UserAuthCache {
}
