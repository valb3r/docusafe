package org.adorsys.documentsafe.layer03business;

import org.adorsys.documentsafe.layer00common.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer02service.types.ReadKeyPassword;
import org.adorsys.documentsafe.layer02service.utils.TestFsBlobStoreFactory;
import org.adorsys.documentsafe.layer03business.impl.DocumentSafeServiceImpl;
import org.adorsys.documentsafe.layer03business.types.DocumentFQN;
import org.adorsys.documentsafe.layer03business.types.UserID;
import org.adorsys.documentsafe.layer03business.types.complex.DSDocument;
import org.adorsys.documentsafe.layer03business.types.complex.UserIDAuth;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by peter on 19.01.18 at 16:25.
 */
public class BusinessTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(BusinessTest.class);
    private final static BlobStoreContextFactory factory = new TestFsBlobStoreFactory();
    public static Set<UserIDAuth> users = new HashSet<>();

    @Before
    public void before() {
        users.clear();
    }

    @After
    public void after() {
        try {
            DocumentSafeService service = new DocumentSafeServiceImpl(factory);
            users.forEach(userIDAuth -> {
                LOGGER.info("AFTER TEST DESTROY " + userIDAuth.getUserID().getValue());
                service.destroyUser(userIDAuth);
            });
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testCreateUser() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuth = new UserIDAuth(new UserID("UserPeter"), new ReadKeyPassword("peterkey"));
        users.add(userIDAuth);
        DocumentSafeService service = new DocumentSafeServiceImpl(factory);
        service.createUser(userIDAuth);
    }

    @Test
    public void loadCDocument() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuth = new UserIDAuth(new UserID("UserPeter"), new ReadKeyPassword("peterkey"));
        users.add(userIDAuth);
        DocumentSafeService service = new DocumentSafeServiceImpl(factory);
        DocumentFQN documentFQN = new DocumentFQN("README.txt");
        service.createUser(userIDAuth);
        DSDocument dsDocument = service.readDocument(userIDAuth, documentFQN);
        LOGGER.debug("retrieved document:" + new String(dsDocument.getDocumentContent().getValue()));
    }

}
