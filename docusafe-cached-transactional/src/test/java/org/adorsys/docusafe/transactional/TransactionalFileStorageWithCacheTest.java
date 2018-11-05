package org.adorsys.docusafe.transactional;

import org.adorsys.docusafe.cached.transactional.exceptions.CacheException;
import org.adorsys.docusafe.cached.transactional.impl.CachedTransactionalDocumentSafeServiceImpl;
import org.adorsys.docusafe.transactional.exceptions.TxNotActiveException;
import org.adorsys.docusafe.transactional.impl.TransactionalDocumentSafeServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 12.07.18 at 12:00.
 */
public class TransactionalFileStorageWithCacheTest extends TransactionalFileStorageTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(TransactionalFileStorageWithCacheTest.class);
    @Before
    public void preTest() {
        TransactionalDocumentSafeService localTransactionalFileStorage = new TransactionalDocumentSafeServiceImpl(requestMemoryContext, dssi);
        this.transactionalFileStorage = new CachedTransactionalDocumentSafeServiceImpl(requestMemoryContext, localTransactionalFileStorage);
    }

    @Test(expected = TxNotActiveException.class)
    @Override
    public void testEndTxTwice() {
        
        super.testEndTxTwice();
    }

    @Test
    @Override
    public void testDelete() {
        // txDeleteFolder not yet implemented in cache layer
    }
}
