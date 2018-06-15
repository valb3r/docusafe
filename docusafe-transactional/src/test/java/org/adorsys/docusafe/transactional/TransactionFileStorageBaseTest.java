package org.adorsys.docusafe.transactional;

import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.docusafe.business.impl.DocumentSafeServiceImpl;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.transactional.impl.TransactionalFileStorageImpl;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.junit.Before;

/**
 * Created by peter on 15.06.18 at 12:52.
 */
public class TransactionFileStorageBaseTest {

    TransactionalFileStorage transactionalFileStorage = null;
    UserIDAuth userIDAuth = null;
    UserIDAuth systemUserIDAuth = null;

    @Before
    public void preTest() {
        ExtendedStoreConnection esc = ExtendedStoreConnectionFactory.get();
        esc.listAllBuckets().forEach(b -> esc.deleteContainer(b));

        transactionalFileStorage = new TransactionalFileStorageImpl(new DocumentSafeServiceImpl(ExtendedStoreConnectionFactory.get()));
        userIDAuth = new UserIDAuth(new UserID("peter"), new ReadKeyPassword("password"));
        systemUserIDAuth = new UserIDAuth(new UserID("system"), new ReadKeyPassword("systemPassword"));
    }

}
