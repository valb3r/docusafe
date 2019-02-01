package org.adorsys.docusafe.transactional;

import org.adorsys.docusafe.cached.transactional.impl.CachedTransactionalDocumentSafeServiceImpl;
import org.adorsys.docusafe.transactional.impl.TransactionalDocumentSafeServiceImpl;
import org.junit.Before;

/**
 * Created by peter on 01.02.19 12:03.
 */
public class MultipleUserSameContextWithCacheTest extends MultipleUserSameContextTest {
    @Before
    public void preTest() {
        TransactionalDocumentSafeService localTransactionalFileStorage = new TransactionalDocumentSafeServiceImpl(requestMemoryContext, dssi);
        this.transactionalFileStorage = new CachedTransactionalDocumentSafeServiceImpl(requestMemoryContext, localTransactionalFileStorage);
    }
}
