package org.adorsys.docusafe.business.impl.caches.hashmap;

import org.adorsys.docusafe.business.impl.caches.UserAuthCache;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.encobject.domain.ReadKeyPassword;

/**
 * Created by peter on 14.08.18 at 16:43.
 */
public class UserAuthCacheHashMapImpl
    extends DocusafeCacheTemplateHashMapImpl <UserID, ReadKeyPassword>
    implements UserAuthCache {
}
