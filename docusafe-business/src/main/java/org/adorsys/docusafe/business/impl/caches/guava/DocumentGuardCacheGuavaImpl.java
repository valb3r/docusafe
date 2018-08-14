package org.adorsys.docusafe.business.impl.caches.guava;

import org.adorsys.docusafe.business.impl.caches.DocumentGuardCache;
import org.adorsys.docusafe.service.impl.PasswordAndDocumentKeyIDWithKeyAndAccessType;

/**
 * Created by peter on 14.08.18 at 16:55.
 */
public class DocumentGuardCacheGuavaImpl
        extends DocusafeCacheTemplateGuavaImpl<String, PasswordAndDocumentKeyIDWithKeyAndAccessType>
        implements DocumentGuardCache {
}
