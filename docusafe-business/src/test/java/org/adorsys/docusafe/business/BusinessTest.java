package org.adorsys.docusafe.business;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.storageconnection.testsuite.ExtendedStoreConnectionFactory;
import org.adorsys.docusafe.business.exceptions.NoWriteAccessException;
import org.adorsys.docusafe.business.impl.DocumentSafeServiceImpl;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.business.utils.GuardUtil;
import org.adorsys.docusafe.business.utils.UserIDUtil;
import org.adorsys.docusafe.service.BucketService;
import org.adorsys.docusafe.service.exceptions.NoDocumentGuardExists;
import org.adorsys.docusafe.service.impl.BucketServiceImpl;
import org.adorsys.docusafe.service.types.AccessType;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.docusafe.service.types.DocumentKeyID;
import org.adorsys.docusafe.service.types.complextypes.BucketContent;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Security;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by peter on 19.01.18 at 16:25.
 */
public class BusinessTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(BusinessTest.class);
    public final static ExtendedStoreConnection extendedStoreConnection = ExtendedStoreConnectionFactory.get();
    private DocumentSafeService service;

    public static Set<UserIDAuth> users = new HashSet<>();

    @Before
    public void before() {
        LOGGER.info("add bouncy castle provider");
        Security.addProvider(new BouncyCastleProvider());
        users.clear();
        service = new DocumentSafeServiceImpl(extendedStoreConnection);
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
    public void deleteDocumentTest() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserID userID = new UserID("UserPeterDelete");
        Assert.assertFalse(service.userExists(userID));
        UserIDAuth userIDAuth = createUser(userID);
        Assert.assertTrue(service.userExists(userID));
        DocumentFQN fqn = new DocumentFQN("README.txt");
        service.readDocument(userIDAuth, fqn);
        Assert.assertTrue(service.documentExists(userIDAuth, fqn));
        service.deleteDocument(userIDAuth, fqn);
        Assert.assertFalse(service.documentExists(userIDAuth, fqn));
        try {
            service.readDocument(userIDAuth, fqn);
        } catch (Exception e) {
            LOGGER.info("Exception expected! Test is fine");
            return;
        }
        throw new BaseException("document is still readable:" + fqn);
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

    @Test
    public void grantAccessToFolder() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuthPeter = createUser(new UserID("peter"), new ReadKeyPassword("keyPasswordForPeter"));
        UserIDAuth userIDAuthFrancis = createUser(new UserID("francis"), new ReadKeyPassword("keyPasswordForFrancis"));
        DocumentFQN documentFQN = new DocumentFQN("first/next/a-new-document.txt");
        DSDocument dsDocument1 = createDocument(userIDAuthPeter, documentFQN);

        DocumentDirectoryFQN documentDirectoryFQN = new DocumentDirectoryFQN("first/next");

        service.grantAccessToUserForFolder(userIDAuthPeter, userIDAuthFrancis.getUserID(), documentDirectoryFQN, AccessType.WRITE);

        DSDocument dsDocument = service.readGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), documentFQN);
        Assert.assertEquals("document content ok", dsDocument1.getDocumentContent(), dsDocument.getDocumentContent());

        service.storeGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), dsDocument);
    }

    @Test(expected = NoWriteAccessException.class)
    public void grantReadAccessToFolder() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuthPeter = createUser(new UserID("peter"), new ReadKeyPassword("keyPasswordForPeter"));
        UserIDAuth userIDAuthFrancis = createUser(new UserID("francis"), new ReadKeyPassword("keyPasswordForFrancis"));
        DocumentFQN documentFQN = new DocumentFQN("first/next/a-new-document.txt");
        DSDocument dsDocument1 = createDocument(userIDAuthPeter, documentFQN);

        DocumentDirectoryFQN documentDirectoryFQN = new DocumentDirectoryFQN("first/next");

        service.grantAccessToUserForFolder(userIDAuthPeter, userIDAuthFrancis.getUserID(), documentDirectoryFQN, AccessType.READ);

        DSDocument dsDocument = service.readGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), documentFQN);
        Assert.assertEquals("document content ok", dsDocument1.getDocumentContent(), dsDocument.getDocumentContent());

        service.storeGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), dsDocument);
    }

    // @Test
    // Scheitert noch, da LinkGuard keinen Key enthält
    public void grantReadAccessToFolderWithLinkedDocument() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        // Anlegen eines Documents
        UserIDAuth userIDAuthPeter = createUser(new UserID("peter"), new ReadKeyPassword("keyPasswordForPeter"));
        DocumentFQN documentFQN = new DocumentFQN("privateFolder/a-new-document.txt");
        DSDocument dsDocument1 = createDocument(userIDAuthPeter, documentFQN);

        // Linken des Documents in einen weiteren folder
        DocumentDirectoryFQN publicFolder = new DocumentDirectoryFQN("publicFolder");
        DocumentFQN linkDocumentFQN = publicFolder.addName("a-new-document.txt");
        service.linkDocument(userIDAuthPeter, documentFQN, linkDocumentFQN);

        // Lesen beider Documente
        DSDocument dsDocumentFromPrivateFolder = service.readDocument(userIDAuthPeter, documentFQN);
        DSDocument dsDocumentFromPublicFolder = service.readDocument(userIDAuthPeter, linkDocumentFQN);

        // Vergleichen der gelesenen Inhalate mit den verschickten Inhalten
        Assert.assertTrue(Arrays.equals(dsDocumentFromPrivateFolder.getDocumentContent().getValue(), dsDocument1.getDocumentContent().getValue()));
        Assert.assertTrue(Arrays.equals(dsDocumentFromPublicFolder.getDocumentContent().getValue(), dsDocument1.getDocumentContent().getValue()));


        // Nun darf auch Francis im Public Folder lesen
        UserIDAuth userIDAuthFrancis = createUser(new UserID("francis"), new ReadKeyPassword("keyPasswordForFrancis"));
        service.grantAccessToUserForFolder(userIDAuthPeter, userIDAuthFrancis.getUserID(), publicFolder, AccessType.READ);
        service.readGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), linkDocumentFQN);

    }

    /**
     * Zunächst erstellt peter ein document.
     * Dann darf Francis das Document auch lesen, aber nicht schreiben.
     * Dann darf er auch schreiben.
     * Schließlich darf er garnicht mehr drauf zugreifen.
     * Zuletzt wird gezeigt, dass Peter das Dokument nach wie vor schreiben darf.
     */
    @Test
    public void tryOverwriteGrantAccessToFolder() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuthPeter = createUser(new UserID("peter"), new ReadKeyPassword("keyPasswordForPeter"));
        UserIDAuth userIDAuthFrancis = createUser(new UserID("francis"), new ReadKeyPassword("keyPasswordForFrancis"));
        DocumentFQN documentFQN = new DocumentFQN("first/next/a-new-document.txt");
        DSDocument dsDocument = createDocument(userIDAuthPeter, documentFQN);

        DocumentDirectoryFQN documentDirectoryFQN = new DocumentDirectoryFQN("first/next");

        Assert.assertEquals("Anzahl der guards", 2, getNumberOfGuards(userIDAuthPeter.getUserID()));
        Assert.assertEquals("Anzahl der guards", 1, getNumberOfGuards(userIDAuthFrancis.getUserID()));

        service.grantAccessToUserForFolder(userIDAuthPeter, userIDAuthFrancis.getUserID(), documentDirectoryFQN, AccessType.READ);
        Assert.assertEquals("Anzahl der guards", 2, getNumberOfGuards(userIDAuthPeter.getUserID()));
        Assert.assertEquals("Anzahl der guards", 2, getNumberOfGuards(userIDAuthFrancis.getUserID()));
        boolean noWriteAccessExceptionCaught = false;
        try {
            service.storeGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), dsDocument);
        } catch (NoWriteAccessException e) {
            LOGGER.debug("NoWriteAccessException was expected. Now we give francis the write access");
            noWriteAccessExceptionCaught = true;

        }
        Assert.assertTrue(noWriteAccessExceptionCaught);
        service.grantAccessToUserForFolder(userIDAuthPeter, userIDAuthFrancis.getUserID(), documentDirectoryFQN, AccessType.WRITE);
        Assert.assertEquals("Anzahl der guards", 2, getNumberOfGuards(userIDAuthPeter.getUserID()));
        Assert.assertEquals("Anzahl der guards", 2, getNumberOfGuards(userIDAuthFrancis.getUserID()));

        service.storeGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), dsDocument);
        service.readGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), documentFQN);
        service.grantAccessToUserForFolder(userIDAuthPeter, userIDAuthFrancis.getUserID(), documentDirectoryFQN, AccessType.READ);
        service.readGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), documentFQN);

        service.grantAccessToUserForFolder(userIDAuthPeter, userIDAuthFrancis.getUserID(), documentDirectoryFQN, AccessType.NONE);
        Assert.assertEquals("Anzahl der guards", 2, getNumberOfGuards(userIDAuthPeter.getUserID()));
        Assert.assertEquals("Anzahl der guards", 1, getNumberOfGuards(userIDAuthFrancis.getUserID()));
        boolean documentGuardExists = true;
        try {
            service.readGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), documentFQN);
        } catch(NoDocumentGuardExists e) {
            documentGuardExists = false;
            LOGGER.debug("Exception was expected");
        }
        Assert.assertFalse("es darf kein dokument guard mehr exisitieren", documentGuardExists);

        DSDocument dsDocument1 = service.readDocument(userIDAuthPeter, documentFQN);
        service.storeDocument(userIDAuthPeter, dsDocument1);
    }


    private int getNumberOfGuards(UserID userID) {
        BucketService bucketService = new BucketServiceImpl(extendedStoreConnection);
        BucketDirectory keyStoreDirectory = UserIDUtil.getKeyStoreDirectory(userID);
        BucketContent bucketContent = bucketService.readDocumentBucket(keyStoreDirectory, ListRecursiveFlag.TRUE);
        int count = 0;
        for (StorageMetadata meta : bucketContent.getContent()) {
            if (meta.getName().endsWith("bucketGuardKey")) {
                count++;
            }
        }
        return count;

    }


    private UserIDAuth createUser(UserID userID) {
        return createUser(userID, new ReadKeyPassword("peterkey"));
    }
    private UserIDAuth createUser() {
        return createUser(new UserID("UserPeter"), new ReadKeyPassword("peterkey"));
    }

    private UserIDAuth createUser(UserID userID, ReadKeyPassword readKeyPassword) {
        UserIDAuth userIDAuth = new UserIDAuth(userID, readKeyPassword);
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
            LOGGER.debug("check no bucket guard exists yet for " + dsDocument.getDocumentFQN());
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
        BucketDirectory homeBucketDirectory = UserIDUtil.getHomeBucketDirectory(userIDAuth.getUserID());
        BucketDirectory keyStoreDirectory = UserIDUtil.getKeyStoreDirectory(userIDAuth.getUserID());
        BucketDirectory bucketDirectory = homeBucketDirectory.append(new BucketPath(dsDocument1Result.getDocumentFQN().getValue())).getBucketDirectory();
        LOGGER.debug("check one bucket guard exists yet for " + bucketDirectory);
        DocumentKeyID documentKeyID = GuardUtil.tryToLoadBucketGuardKeyFile(
                new BucketServiceImpl(extendedStoreConnection),
                keyStoreDirectory,
                bucketDirectory);
        Assert.assertNotNull(documentKeyID);
        return dsDocument1Result;
    }

    private void checkGuardsForDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN, boolean exists) {
        // check, there guards
        BucketDirectory homeBucketDirectory = UserIDUtil.getHomeBucketDirectory(userIDAuth.getUserID());
        BucketDirectory keyStoreDirectory = UserIDUtil.getKeyStoreDirectory(userIDAuth.getUserID());
        BucketDirectory bucketDirectory = homeBucketDirectory.append(new BucketPath(documentFQN.getValue())).getBucketDirectory();
        DocumentKeyID documentKeyID0 = GuardUtil.tryToLoadBucketGuardKeyFile(
                new BucketServiceImpl(extendedStoreConnection),
                keyStoreDirectory,
                bucketDirectory);
        if (exists) {
            Assert.assertNotNull(documentKeyID0);
        } else {
            Assert.assertNull(documentKeyID0);
        }
    }

    private static void sleep(int secs) {
        try {
            LOGGER.info("SLEEP FOR " + secs + " secs");
            Thread.sleep(secs * 1000);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

}
