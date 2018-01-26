package org.adorsys.documentsafe.layer03business;

import org.adorsys.documentsafe.layer00common.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer01persistence.types.ListRecursiveFlag;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.KeyStoreDirectory;
import org.adorsys.documentsafe.layer02service.BucketService;
import org.adorsys.documentsafe.layer02service.impl.BucketServiceImpl;
import org.adorsys.documentsafe.layer02service.types.DocumentContent;
import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer02service.types.ReadKeyPassword;
import org.adorsys.documentsafe.layer02service.types.complextypes.BucketContent;
import org.adorsys.documentsafe.layer02service.utils.TestFsBlobStoreFactory;
import org.adorsys.documentsafe.layer03business.impl.DocumentSafeServiceImpl;
import org.adorsys.documentsafe.layer03business.types.UserHomeBucketPath;
import org.adorsys.documentsafe.layer03business.types.UserID;
import org.adorsys.documentsafe.layer03business.types.complex.DSDocument;
import org.adorsys.documentsafe.layer03business.types.complex.DocumentFQN;
import org.adorsys.documentsafe.layer03business.types.complex.UserIDAuth;
import org.adorsys.documentsafe.layer03business.utils.GuardUtil;
import org.adorsys.documentsafe.layer03business.utils.UserIDUtil;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Security;
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
        LOGGER.info("add bouncy castle provider");
        Security.addProvider(new BouncyCastleProvider());
        users.clear();
    }

    @After
    public void after() {
        try {
            DocumentSafeService service = new DocumentSafeServiceImpl(factory);
            users.forEach(userIDAuth -> {
                LOGGER.debug("AFTER TEST DESTROY " + userIDAuth.getUserID().getValue());
                service.destroyUser(userIDAuth);
            });
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testCreateUser() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuth = new UserIDAuth(new UserID("UserPeter"), new ReadKeyPassword("peterkey"));
        users.add(userIDAuth);
        DocumentSafeService service = new DocumentSafeServiceImpl(factory);
        service.createUser(userIDAuth);

        Assert.assertEquals("Anzahl der guards muss genau 1 sein", 1, getNumberOfGuards(userIDAuth.getUserID()));
    }

    @Test
    public void loadDSDocument() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuth = new UserIDAuth(new UserID("UserPeter"), new ReadKeyPassword("peterkey"));
        users.add(userIDAuth);
        DocumentSafeService service = new DocumentSafeServiceImpl(factory);
        DocumentFQN documentFQN = new DocumentFQN("README.txt");
        service.createUser(userIDAuth);
        DSDocument dsDocument = service.readDocument(userIDAuth, documentFQN);
        LOGGER.debug("retrieved document:" + new String(dsDocument.getDocumentContent().getValue()));
        Assert.assertEquals("Anzahl der guards muss genau 1 sein", 1, getNumberOfGuards(userIDAuth.getUserID()));
    }

    @Test
    public void storeDSDocumentInANewFolder() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuth = new UserIDAuth(new UserID("UserPeter"), new ReadKeyPassword("peterkey"));
        users.add(userIDAuth);
        DocumentSafeService service = new DocumentSafeServiceImpl(factory);
        service.createUser(userIDAuth);
        Assert.assertEquals("Anzahl der guards muss genau 1 sein", 1, getNumberOfGuards(userIDAuth.getUserID()));

        DocumentFQN documentFQN = new DocumentFQN("first/next/A new Document.txt");
        DocumentContent documentContent = new DocumentContent("Einfach nur a bisserl Text".getBytes());
        DSDocument dsDocument1 = new DSDocument(documentFQN, documentContent, null);

        // check, there exists no guard yet
        UserHomeBucketPath homeBucketPath = UserIDUtil.getHomeBucketPath(userIDAuth.getUserID());
        DocumentKeyID documentKeyID0 = GuardUtil.tryToLoadBucketGuardKeyFile(new BucketServiceImpl(factory), UserIDUtil.getKeyStoreDirectory(userIDAuth.getUserID()), homeBucketPath.append(new BucketPath(dsDocument1.getDocumentFQN().getValue())));
        Assert.assertNull(documentKeyID0);

        service.storeDocument(userIDAuth, dsDocument1);
        DSDocument dsDocument1Result = service.readDocument(userIDAuth, dsDocument1.getDocumentFQN());
        LOGGER.debug("retrieved document:" + new String(dsDocument1Result.getDocumentContent().getValue()));

        // check, there exists exaclty one guard for the user
        Assert.assertEquals("Anzahl der guards muss genau 2 sein", 2, getNumberOfGuards(userIDAuth.getUserID()));

        DocumentFQN document2FQN = new DocumentFQN("first/next/Another new Document.txt");
        DSDocument dsDocument2 = new DSDocument(document2FQN, dsDocument1.getDocumentContent(), null);
        service.storeDocument(userIDAuth, dsDocument2);
        DSDocument dsDocument2Result = service.readDocument(userIDAuth, dsDocument2.getDocumentFQN());
        LOGGER.debug("retrieved document:" + new String(dsDocument2Result.getDocumentContent().getValue()));
    }

    private int getNumberOfGuards(UserID userID) {
        BucketService bucketService = new BucketServiceImpl(factory);
        KeyStoreDirectory keyStoreDirectory = UserIDUtil.getKeyStoreDirectory(userID);
        BucketContent bucketContent = bucketService.readDocumentBucket(keyStoreDirectory, ListRecursiveFlag.TRUE);
        int count = 0;
        for (StorageMetadata meta : bucketContent.getStrippedContent()) {
            if (meta.getName().endsWith("bucketGuardKey")) {
                count ++;
            }
        }
        return count;

    }

}
