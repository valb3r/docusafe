package org.adorsys.docusafe.business.impl;

import org.adorsys.docusafe.service.impl.DocumentKeyIDMap;
import org.adorsys.docusafe.service.impl.GuardMap;

/**
 * Created by peter on 10.08.18 at 13:54.
 */
public interface DocusafeCache {
    public UserAuthCache getUserAuthCache();
    public GuardMap getGuardCache();
    public DocumentKeyIDMap getDocumentKeyIDCache();
}
