package org.adorsys.docusafe.business.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.impl.caches.DocumentGuardCache;
import org.adorsys.docusafe.business.impl.caches.DocumentKeyIDCache;
import org.adorsys.docusafe.business.impl.caches.UserAuthCache;
import org.adorsys.docusafe.business.impl.caches.guava.DocumentGuardCacheGuavaImpl;
import org.adorsys.docusafe.business.impl.caches.guava.DocumentKeyIDCacheGuavaImpl;
import org.adorsys.docusafe.business.impl.caches.guava.UserAuthCacheGuavaImpl;
import org.adorsys.docusafe.business.impl.caches.hashmap.DocumentGuardCacheHashMapImpl;
import org.adorsys.docusafe.business.impl.caches.hashmap.DocumentKeyIDCacheHashMapImpl;
import org.adorsys.docusafe.business.impl.caches.hashmap.UserAuthCacheHashMapImpl;

/**
 * Created by peter on 26.06.18 at 17:56.
 */
public class DocusafeCacheWrapperImpl implements DocusafeCacheWrapper {
    private UserAuthCache userAuthCache;
    private DocumentGuardCache guardCache;
    private DocumentKeyIDCache documentKeyIDCache;

    public DocusafeCacheWrapperImpl(CacheType cacheType) {
        switch (cacheType) {
            case GUAVA: {
                userAuthCache = new UserAuthCacheGuavaImpl();
                guardCache = new DocumentGuardCacheGuavaImpl();
                documentKeyIDCache = new DocumentKeyIDCacheGuavaImpl();
                break;
            }
            case HASH_MAP: {
                userAuthCache = new UserAuthCacheHashMapImpl();
                guardCache = new DocumentGuardCacheHashMapImpl();
                documentKeyIDCache = new DocumentKeyIDCacheHashMapImpl();
                break;
            }
            default:
                throw new BaseException("Programming Error missing switch for " + cacheType);
        }
    }

    @Override
    public UserAuthCache getUserAuthCache() {
        return userAuthCache;
    }

    @Override
    public DocumentGuardCache getDocumentGuardCache() {
        return guardCache;
    }

    @Override
    public DocumentKeyIDCache getDocumentKeyIDCache() {
        return documentKeyIDCache;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DOCUSAFE_CACHE WRAPPER");
        sb.append(" ");
        sb.append("number of users: " + getUserAuthCache().size());
        sb.append(" ");
        sb.append("number of guards: " + getDocumentGuardCache().size());
        sb.append(" ");
        sb.append("number of doc keys: " + getDocumentKeyIDCache().size());
        return sb.toString();
    }
}
