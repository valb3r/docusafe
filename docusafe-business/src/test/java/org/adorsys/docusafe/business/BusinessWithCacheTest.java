package org.adorsys.docusafe.business;

import org.adorsys.docusafe.business.impl.DocumentSafeServiceImpl;
import org.adorsys.docusafe.business.impl.WithCache;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 28.06.18 at 16:02.
 */
@SuppressWarnings("Duplicates")
public class BusinessWithCacheTest extends BusinessTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(BusinessWithCacheTest.class);


    @Before
    public void before() {
        withCache = WithCache.TRUE;
        super.before();
    }

    @Override
    public void after() {
        try {
            super.after();
        } finally {
            LOGGER.debug(DocumentSafeServiceImpl.showCache(service));
        }
    }
}
