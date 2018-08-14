package org.adorsys.docusafe.business.impl;

/**
 * Created by peter on 10.08.18 at 13:54.
 */
public interface DocusafeCacheWrapper {
    UserAuthCache getUserAuthCache();
    DocumentGuardCache getDocumentGuardCache();
    DocumentKeyIDCache getDocumentKeyIDCache();
}
