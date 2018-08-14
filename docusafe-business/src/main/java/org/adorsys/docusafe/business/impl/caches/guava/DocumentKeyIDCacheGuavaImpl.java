package org.adorsys.docusafe.business.impl.caches.guava;

import org.adorsys.docusafe.business.impl.caches.DocumentKeyIDCache;
import org.adorsys.docusafe.service.types.DocumentKeyID;
import org.adorsys.encobject.complextypes.BucketDirectory;

/**
 * Created by peter on 14.08.18 at 17:26.
 */
public class DocumentKeyIDCacheGuavaImpl
        extends DocusafeCacheTemplateGuavaImpl<BucketDirectory, DocumentKeyID>
        implements DocumentKeyIDCache {
}
