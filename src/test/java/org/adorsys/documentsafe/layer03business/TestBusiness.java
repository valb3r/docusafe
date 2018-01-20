package org.adorsys.documentsafe.layer03business;

import org.adorsys.documentsafe.layer00common.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer02service.types.ReadKeyPassword;
import org.adorsys.documentsafe.layer02service.utils.TestFsBlobStoreFactory;
import org.adorsys.documentsafe.layer03business.impl.DocumentSafeServiceImpl;
import org.adorsys.documentsafe.layer03business.types.DocumentFQN;
import org.adorsys.documentsafe.layer03business.types.UserID;
import org.adorsys.documentsafe.layer03business.types.complex.UserIDAuth;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by peter on 19.01.18 at 16:25.
 */
public class TestBusiness {
    private final static Logger LOGGER = LoggerFactory.getLogger(TestBusiness.class);
    private final static BlobStoreContextFactory factory = new TestFsBlobStoreFactory();
    public static Set<UserIDAuth> users = new HashSet<>();

    @After
    public void after() {
        try {
            DocumentSafeService service = new DocumentSafeServiceImpl(factory);
            users.forEach(userIDAuth -> service.destroyUser(userIDAuth));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testCreateUser() {
        DocumentSafeService service = new DocumentSafeServiceImpl(factory);
        UserIDAuth userIDAuth = new UserIDAuth(new UserID("UserPeter"), new ReadKeyPassword("peterkey"));
        users.add(userIDAuth);
        service.createUser(userIDAuth);
    }

    @Test
    public void loadCreateUser() {
        DocumentSafeService service = new DocumentSafeServiceImpl(factory);
        UserIDAuth userIDAuth = new UserIDAuth(new UserID("UserPeter"), new ReadKeyPassword("peterkey"));
        DocumentFQN documentFQN = new DocumentFQN("README.txt");
        service.readDocument(userIDAuth, documentFQN);
    }

}
