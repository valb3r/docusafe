package org.adorsys.docusafe.business.impl;

import org.adorsys.docusafe.business.impl.caches.DocumentGuardCacheHashMapImpl;

/**
 * Created by peter on 26.06.18 at 17:56.
 */
public class DocusafeCacheWrapperImpl implements DocusafeCacheWrapper {
    private UserAuthCache userAuthCache;
    private DocumentGuardCache guardCache;
    private DocumentKeyIDCache documentKeyIDCache;

    public DocusafeCacheWrapperImpl() {
        userAuthCache = new UserAuthCache();
        guardCache = new DocumentGuardCacheHashMapImpl();
        documentKeyIDCache = new DocumentKeyIDCache();
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
        sb.append("number of users: " + getUserAuthCache().keySet().size());
        sb.append(" ");
        sb.append("number of guards: " + getDocumentGuardCache().toString());
        sb.append(" ");
        sb.append("number of doc keys: " + getDocumentKeyIDCache().keySet().size());
        return sb.toString();
    }
}
