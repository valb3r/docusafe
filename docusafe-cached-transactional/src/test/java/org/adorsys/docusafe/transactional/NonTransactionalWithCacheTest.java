package org.adorsys.docusafe.transactional;

import org.adorsys.docusafe.cached.transactional.impl.CachedTransactionalDocumentSafeServiceImpl;
import org.adorsys.docusafe.transactional.impl.TransactionalDocumentSafeServiceImpl;
import org.junit.Before;

/**
 * Created by peter on 12.07.18 at 11:56.
 */
public class NonTransactionalWithCacheTest extends NonTransactionalTest {
    @Before
    public void preTest() {
        TransactionalDocumentSafeService localTransactionalFileStorage = new TransactionalDocumentSafeServiceImpl(requestMemoryContext, dssi);
        this.transactionalFileStorage = new CachedTransactionalDocumentSafeServiceImpl(requestMemoryContext, localTransactionalFileStorage);
    }
}
