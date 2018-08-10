package org.adorsys.docusafe.business;

import org.adorsys.docusafe.business.impl.DocusafeCacheImpl;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Security;

/**
 * Created by peter on 28.06.18 at 16:21.
 */
@SuppressWarnings("Duplicates")
public class BusinessUnencryptedWithCacheTest extends BusinessUnencryptedTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(BusinessUnencryptedWithCacheTest.class);

    @BeforeClass
    static public void beforeClass() {
        LOGGER.debug("add bouncy castle provider");
        Security.addProvider(new BouncyCastleProvider());
        LOGGER.debug("clear whole database");
        extendedStoreConnection.listAllBuckets().forEach(bucket -> extendedStoreConnection.deleteContainer(bucket));
    }

    @Before
    public void before() {
        super.before();
    }

    @Override
    public void after() {
        try {
            super.after();
        } finally {
            LOGGER.info(DocusafeCacheImpl.toString(mc));
            service.setMemoryContext(null);
            mc = null;
        }
    }
}
