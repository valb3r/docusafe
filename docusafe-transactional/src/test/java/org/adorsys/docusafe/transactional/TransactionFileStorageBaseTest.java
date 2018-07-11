package org.adorsys.docusafe.transactional;

import com.googlecode.catchexception.CatchException;
import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.docusafe.business.impl.DocumentSafeServiceImpl;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.transactional.impl.TransactionalFileStorageImpl;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.junit.After;
import org.junit.Before;

/**
 * Created by peter on 15.06.18 at 12:52.
 */
public class TransactionFileStorageBaseTest {

    RequestMemoryContext requestMemoryContext = new SimpleRequestMemoryContextImpl();
    DocumentSafeServiceImpl dssi = new DocumentSafeServiceImpl(ExtendedStoreConnectionFactory.get());
    TransactionalFileStorage transactionalFileStorage = new TransactionalFileStorageImpl(requestMemoryContext, dssi);
    UserIDAuth userIDAuth = new UserIDAuth(new UserID("peter"), new ReadKeyPassword("password"));
    UserIDAuth systemUserIDAuth = new UserIDAuth(new UserID("system"), new ReadKeyPassword("systemPassword"));

    @Before
    public void preTest() {

    }

    @After
    public void after() {
        CatchException.catchException(() -> transactionalFileStorage.destroyUser(userIDAuth));
        CatchException.catchException(() -> transactionalFileStorage.destroyUser(systemUserIDAuth));
    }

}
