package org.adorsys.docusafe.cached.transactional.impl;

import org.adorsys.docusafe.transactional.TransactionalDocumentSafeServiceTest;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 22.02.19 19:12.
 */
public class CachedTransactionalDocumentSafeServiceTest extends TransactionalDocumentSafeServiceTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(CachedTransactionalDocumentSafeServiceTest.class);
    private TransactionalDocumentSafeServiceTestWrapper wrapper = null;

    @Before
    public void preTestCached() {
        LOGGER.debug("preTestCached changed transactionalDocumentSafeService");

        // erst mal machen wir aus der transactionalDocumentSafeService eine cachedTransactionalDocumentSafeService;
        transactionalDocumentSafeService = new CachedTransactionalDocumentSafeServiceImpl(requestMemoryContext, transactionalDocumentSafeService, dss);

        // diese wrappen wir
        wrapper = new TransactionalDocumentSafeServiceTestWrapper(transactionalDocumentSafeService);

        // und die gewrappte geben wir an den test
        transactionalDocumentSafeService = wrapper;

        // und der nichttransaktionale teil wird ebenfalls mit dem Wrapper versorgt
        nonTransactionalDocumentSafeService = transactionalDocumentSafeService;
    }

    @After
    public void afterTestCached() {
        LOGGER.debug("afterTestCached " + transactionalDocumentSafeService.toString());
    }
}
