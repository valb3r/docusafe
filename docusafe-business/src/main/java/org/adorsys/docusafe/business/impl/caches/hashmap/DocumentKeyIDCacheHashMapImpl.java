package org.adorsys.docusafe.business.impl.caches.hashmap;

import org.adorsys.docusafe.business.impl.caches.DocumentKeyIDCache;
import org.adorsys.docusafe.service.types.DocumentKeyID;
import org.adorsys.encobject.complextypes.BucketDirectory;

/**
 * Created by peter on 14.08.18 at 16:19.
 */
public class DocumentKeyIDCacheHashMapImpl
        extends DocusafeCacheTemplateHashMapImpl<BucketDirectory, DocumentKeyID>
        implements DocumentKeyIDCache {
}