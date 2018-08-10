package org.adorsys.docusafe.business.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.service.impl.DocumentKeyIDMap;
import org.adorsys.docusafe.service.impl.GuardMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by peter on 26.06.18 at 17:56.
 */
public class DocusafeCacheImpl implements DocusafeCache {
    private UserAuthCache userAuthCache = new UserAuthCache();
    private GuardMap guardCache = new GuardMap();
    private DocumentKeyIDMap documentKeyIDCache = new DocumentKeyIDMap();

    @Override
    public UserAuthCache getUserAuthCache() {
        return userAuthCache;
    }

    @Override
    public GuardMap getGuardCache() {
        return guardCache;
    }

    @Override
    public DocumentKeyIDMap getDocumentKeyIDCache() {
        return documentKeyIDCache;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DOCUSAFE_CACHE");
        sb.append("\n");
        sb.append("number of users   :" + getUserAuthCache().keySet().size());
        sb.append("number of guards  :" + getGuardCache().keySet().size());
        sb.append("number of doc keys:" + getDocumentKeyIDCache().keySet().size());
        return sb.toString();
    }
}
