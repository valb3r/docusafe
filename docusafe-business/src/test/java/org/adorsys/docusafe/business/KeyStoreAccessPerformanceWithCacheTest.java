package org.adorsys.docusafe.business;

import org.adorsys.docusafe.business.impl.DocusafeCacheImpl;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Security;

/**
 * Created by peter on 12.07.18 at 13:46.
 */
@SuppressWarnings("Duplicates")
public class KeyStoreAccessPerformanceWithCacheTest extends KeyStoreAccessPerformanceTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(KeyStoreAccessPerformanceWithCacheTest.class);
    private MemoryContext mc;

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
        mc = new DocusafeCacheImpl();
        service.setMemoryContext(mc);
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
