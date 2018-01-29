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
    private DocumentSafeService service;

    public static Set<UserIDAuth> users = new HashSet<>();

    @Before
    public void before() {
        LOGGER.info("add bouncy castle provider");
        Security.addProvider(new BouncyCastleProvider());
        users.clear();
        service = new DocumentSafeServiceImpl(factory);
    }

    @After
    public void after() {
        try {
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
        UserIDAuth userIDAuth = createUser();
        Assert.assertEquals("Anzahl der guards muss 1 betragen", 1, getNumberOfGuards(userIDAuth.getUserID()));
    }


    @Test
    public void loadDSDocument() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuth = createUser();
        checkGuardsForDocument(userIDAuth, new DocumentFQN("README.txt"), true);
        Assert.assertEquals("Anzahl der guards muss 1 betragen", 1, getNumberOfGuards(userIDAuth.getUserID()));
    }

    @Test
    public void storeDSDocumentInANewFolder() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuth = createUser();
        Assert.assertEquals("Anzahl der guards", 1, getNumberOfGuards(userIDAuth.getUserID()));

        DocumentFQN documentFQN = new DocumentFQN("first/next/a-new-document.txt");
        checkGuardsForDocument(userIDAuth, documentFQN, false);
        DSDocument dsDocument1 = createDocument(userIDAuth, documentFQN);
        checkGuardsForDocument(userIDAuth, documentFQN, true);
        Assert.assertEquals("Anzahl der guards", 2, getNumberOfGuards(userIDAuth.getUserID()));
        readDocument(userIDAuth, documentFQN, dsDocument1.getDocumentContent());

        DSDocument dsDocument2 = createDocument(userIDAuth, new DocumentFQN("first/next/another-new-document.txt"));
        readDocument(userIDAuth, dsDocument2.getDocumentFQN(), dsDocument2.getDocumentContent());
        checkGuardsForDocument(userIDAuth, documentFQN, true);
        Assert.assertEquals("Anzahl der guards", 2, getNumberOfGuards(userIDAuth.getUserID()));
    }



    @Test
    public void linkDocument() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuth = createUser();

        DocumentFQN documentFQN = new DocumentFQN("first/next/a-new-document.txt");
        checkGuardsForDocument(userIDAuth, documentFQN, false);
        Assert.assertEquals("Anzahl der guards", 1, getNumberOfGuards(userIDAuth.getUserID()));
        DSDocument dsDocument = createDocument(userIDAuth, documentFQN);
        checkGuardsForDocument(userIDAuth, documentFQN, true);
        Assert.assertEquals("Anzahl der guards", 2, getNumberOfGuards(userIDAuth.getUserID()));

        DocumentFQN linkDocumentFQN = new DocumentFQN("newBucket/a-new-document.txt");
        checkGuardsForDocument(userIDAuth, linkDocumentFQN, false);
        service.linkDocument(userIDAuth, documentFQN, linkDocumentFQN);
        checkGuardsForDocument(userIDAuth, linkDocumentFQN, true);
        Assert.assertEquals("Anzahl der guards", 3, getNumberOfGuards(userIDAuth.getUserID()));

        readDocument(userIDAuth, linkDocumentFQN, dsDocument.getDocumentContent());

        Assert.assertEquals("Anzahl der guards", 3, getNumberOfGuards(userIDAuth.getUserID()));
    }

    private int getNumberOfGuards(UserID userID) {
        BucketService bucketService = new BucketServiceImpl(factory);
        KeyStoreDirectory keyStoreDirectory = UserIDUtil.getKeyStoreDirectory(userID);
        BucketContent bucketContent = bucketService.readDocumentBucket(keyStoreDirectory, ListRecursiveFlag.TRUE);
        int count = 0;
        for (StorageMetadata meta : bucketContent.getStrippedContent()) {
            if (meta.getName().endsWith("bucketGuardKey")) {
                count++;
            }
        }
        return count;

    }

    private UserIDAuth createUser() {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID("UserPeter"), new ReadKeyPassword("peterkey"));
        users.add(userIDAuth);
        service.createUser(userIDAuth);
        Assert.assertEquals("Anzahl der guards muss genau 1 sein", 1, getNumberOfGuards(userIDAuth.getUserID()));
        return userIDAuth;
    }

    private DSDocument createDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        DSDocument dsDocument;
        {
            DocumentContent documentContent = new DocumentContent("Einfach nur a bisserl Text".getBytes());
            dsDocument = new DSDocument(documentFQN, documentContent, null);

            // check, there exists no guard yet
            UserHomeBucketPath homeBucketPath = UserIDUtil.getHomeBucketPath(userIDAuth.getUserID());
            KeyStoreDirectory keyStoreDirectory = UserIDUtil.getKeyStoreDirectory(userIDAuth.getUserID());
            BucketPath bucketPath = homeBucketPath.append(new BucketPath(dsDocument.getDocumentFQN().getValue()).getBucketDirectory());
            LOGGER.debug("check no bucket guard exists yet for " + bucketPath);
            service.storeDocument(userIDAuth, dsDocument);
        }
        return dsDocument;
    }

    private DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN, DocumentContent documentContent) {
        DSDocument dsDocument1Result = service.readDocument(userIDAuth, documentFQN);
        LOGGER.debug("original  document:" + new String(documentContent.getValue()));
        LOGGER.debug("retrieved document:" + new String(dsDocument1Result.getDocumentContent().getValue()));
        Assert.assertEquals("document content ok", documentContent, dsDocument1Result.getDocumentContent());

        // check, there guards
        UserHomeBucketPath homeBucketPath = UserIDUtil.getHomeBucketPath(userIDAuth.getUserID());
        KeyStoreDirectory keyStoreDirectory = UserIDUtil.getKeyStoreDirectory(userIDAuth.getUserID());
        BucketPath bucketPath = homeBucketPath.append(new BucketPath(dsDocument1Result.getDocumentFQN().getValue()).getBucketDirectory());
        LOGGER.debug("check one bucket guard exists yet for " + bucketPath);
        DocumentKeyID documentKeyID = GuardUtil.tryToLoadBucketGuardKeyFile(
                new BucketServiceImpl(factory),
                keyStoreDirectory,
                bucketPath);
        Assert.assertNotNull(documentKeyID);
        return dsDocument1Result;
    }

    private void checkGuardsForDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN, boolean exists) {
        // check, there guards
        UserHomeBucketPath homeBucketPath = UserIDUtil.getHomeBucketPath(userIDAuth.getUserID());
        KeyStoreDirectory keyStoreDirectory = UserIDUtil.getKeyStoreDirectory(userIDAuth.getUserID());
        BucketPath bucketPath = homeBucketPath.append(new BucketPath(documentFQN.getValue()).getBucketDirectory());
        DocumentKeyID documentKeyID0 = GuardUtil.tryToLoadBucketGuardKeyFile(
                new BucketServiceImpl(factory),
                keyStoreDirectory,
                bucketPath);
        if (exists) {
            Assert.assertNotNull(documentKeyID0);
        } else {
            Assert.assertNull(documentKeyID0);
        }
    }


}
