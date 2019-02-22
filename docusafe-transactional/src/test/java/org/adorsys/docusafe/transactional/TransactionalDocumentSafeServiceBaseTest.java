package org.adorsys.docusafe.transactional;

import com.googlecode.catchexception.CatchException;
import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.docusafe.business.impl.DocumentSafeServiceImpl;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.transactional.impl.TransactionalDocumentSafeServiceImpl;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 15.06.18 at 12:52.
 */
public class TransactionalDocumentSafeServiceBaseTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(TransactionalDocumentSafeServiceBaseTest.class);

    protected SimpleRequestMemoryContextImpl requestMemoryContext = new SimpleRequestMemoryContextImpl();
    protected DocumentSafeServiceImpl dssi = new DocumentSafeServiceImpl(ExtendedStoreConnectionFactory.get());
    protected TransactionalDocumentSafeService transactionalDocumentSafeService = new TransactionalDocumentSafeServiceImpl(requestMemoryContext, dssi);
    protected NonTransactionalDocumentSafeService nonTransactionalDocumentSafeService = transactionalDocumentSafeService;
    protected UserIDAuth userIDAuth = new UserIDAuth(new UserID("peter"), new ReadKeyPassword("password"));
    protected UserIDAuth systemUserIDAuth = new UserIDAuth(new UserID("system"), new ReadKeyPassword("systemPassword"));

    @Before
    public void preTestBase() {
        LOGGER.debug("preTestBase");
        ExtendedStoreConnection esc = ExtendedStoreConnectionFactory.get();
        esc.listAllBuckets().forEach(bucket -> esc.deleteContainer(bucket));

        LOGGER.debug("TransactionalDcoumentSafeService is " + transactionalDocumentSafeService.getClass().getName());
    }

    @After
    public void afterTestBase() {
        LOGGER.debug("afterTestBase");
        CatchException.catchException(() -> transactionalDocumentSafeService.destroyUser(userIDAuth));
        CatchException.catchException(() -> transactionalDocumentSafeService.destroyUser(systemUserIDAuth));
    }

}
