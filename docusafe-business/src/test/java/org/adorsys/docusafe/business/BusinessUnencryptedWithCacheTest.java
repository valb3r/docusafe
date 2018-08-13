package org.adorsys.docusafe.business;

import org.adorsys.docusafe.business.impl.WithCache;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 28.06.18 at 16:21.
 */
@SuppressWarnings("Duplicates")
public class BusinessUnencryptedWithCacheTest extends BusinessUnencryptedTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(BusinessUnencryptedWithCacheTest.class);

    @Before
    public void before() {
        withCache = WithCache.TRUE;
        super.before();
    }

}
