package org.adorsys.docusafe.business;

import org.adorsys.docusafe.business.impl.WithCache;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 12.07.18 at 13:46.
 */
@SuppressWarnings("Duplicates")
public class KeyStoreAccessPerformanceWithCacheTest extends KeyStoreAccessPerformanceTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(KeyStoreAccessPerformanceWithCacheTest.class);

    @Before
    public void before() {
        withCache = WithCache.TRUE;
        super.before();
    }

}
