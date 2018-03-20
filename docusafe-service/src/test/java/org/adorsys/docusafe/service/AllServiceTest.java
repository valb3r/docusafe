package org.adorsys.docusafe.service;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.storageconnection.testsuite.ExtendedStoreConnectionFactory;
import org.adorsys.cryptoutils.utils.HexUtil;
import org.adorsys.docusafe.service.types.AccessType;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.docusafe.service.types.complextypes.BucketContent;
import org.adorsys.docusafe.service.types.complextypes.DocumentBucketPath;
import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKeyAndAccessType;
import org.adorsys.docusafe.service.utils.ShowKeyStore;
import org.adorsys.docusafe.service.utils.TestKeyUtils;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.KeyStoreAccess;
import org.adorsys.encobject.domain.KeyStoreAuth;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.adorsys.encobject.domain.ReadStorePassword;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.StorageType;
import org.adorsys.encobject.exceptions.FileExistsException;
import org.adorsys.encobject.exceptions.KeyStoreExistsException;
import org.adorsys.encobject.service.api.ContainerPersistence;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.impl.ContainerPersistenceImpl;
import org.adorsys.encobject.service.impl.generator.KeyStoreCreationConfigImpl;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.adorsys.encobject.types.OverwriteFlag;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by peter on 04.01.18.
 */
public class AllServiceTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(AllServiceTest.class);
    private final static ExtendedStoreConnection extendedStoreConnection = ExtendedStoreConnectionFactory.get();
    public static Set<BucketDirectory> buckets = new HashSet<>();

    @Before
    public void before() {
        TestKeyUtils.turnOffEncPolicy();
        buckets.clear();
    }

    @After
    public void after() {
        try {
            ContainerPersistence containerPersistence = new ContainerPersistenceImpl(extendedStoreConnection);
            for (BucketDirectory bucket : buckets) {
                LOGGER.debug("AFTER TEST: DELETE BUCKET " + bucket);
                containerPersistence.deleteContainer(bucket);
            }
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }

    @Test
    public void testCreateBucketPath() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        try {
            BucketDirectory bp = new BucketDirectory("abc1/2/3");
            ContainerPersistence containerPersistence = new ContainerPersistenceImpl(extendedStoreConnection);
            containerPersistence.createContainer(bp);
            buckets.add(bp);
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testCreateKeyStore() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        try {
            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff = new KeyStoreServiceTest(extendedStoreConnection).createKeyStore();
            Assert.assertEquals("Number of Entries", 15, keyStoreStuff.keyStore.size());
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    @Test(expected = KeyStoreExistsException.class)
    public void testCreateKeyStoreTwice() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        try {
            KeyStoreServiceTest keyStoreServiceTest = new KeyStoreServiceTest(extendedStoreConnection);
            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff = keyStoreServiceTest.createKeyStore();
            Assert.assertEquals("Number of Entries", 15, keyStoreStuff.keyStore.size());
            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff2 = keyStoreServiceTest.createKeyStore();
            Assert.assertEquals("Number of Entries", 15, keyStoreStuff2.keyStore.size());
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testCreateKeyStoreAndDocumentGuard() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        try {
            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff = new KeyStoreServiceTest(extendedStoreConnection).createKeyStore();
            new DocumentGuardServiceTest(extendedStoreConnection).testCreateSymmetricDocumentGuard(
                    keyStoreStuff.keyStoreAccess,
                    AccessType.WRITE);
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testCreateKeyStoreAndDocumentGuardAndLoadDocumentGuard() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        try {
            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff = new KeyStoreServiceTest(extendedStoreConnection).createKeyStore();
            DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest(extendedStoreConnection);
            DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuff = documentGuardServiceTest.testCreateSymmetricDocumentGuard(
                    keyStoreStuff.keyStoreAccess, AccessType.WRITE);
            DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = documentGuardServiceTest.testLoadDocumentGuard(
                    keyStoreStuff.keyStoreAccess,
                    documentGuardStuff.documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKeyID());

            LOGGER.debug("DocumentKey is " + HexUtil.convertBytesToHexString(documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKey().getSecretKey().getEncoded()));
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testWrongKeyLoop() {
        if (System.getProperty("loop") == null) {
            LOGGER.info("TEST WRONG LOOP IGNROED. PLEASE RUN WITH -Dloop");
            return;
        }
        try {
            int i = 0;
            boolean foundFirstException = false;
            boolean foundSecondException = false;
            FileUtils.deleteDirectory(new File("target/filesystemstorage"));
            while (!foundFirstException || !foundSecondException) {
                buckets.clear();
                int e = testCreateKeyStoreAndDocumentGuardAndTryToLoadDocumentGuardWithWrongKey("loop" + i);
                if (e == 1) {
                    if (!foundFirstException) {
                        LOGGER.info("========================================");
                        LOGGER.info("FOUND EXCEPTION 1 WITH CONTAINERID " + i);
                        LOGGER.info("========================================");
                        foundFirstException = true;
                    } else {
                        after();
                    }
                }
                if (e == 2) {
                    if (!foundSecondException) {
                        LOGGER.info("========================================");
                        LOGGER.info("FOUND EXCEPTION 2 WITH CONTAINERID " + i);
                        LOGGER.info("========================================");
                        foundSecondException = true;
                    } else {
                        after();
                    }
                }
                i = i + 1;
            }
            // avoid removing result
            buckets.clear();
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testCreateKeyStoreAndDocumentGuardAndTryToLoadDocumentGuardWithWrongKey() {
        testCreateKeyStoreAndDocumentGuardAndTryToLoadDocumentGuardWithWrongKey("testwrongkey");
    }

    private int testCreateKeyStoreAndDocumentGuardAndTryToLoadDocumentGuardWithWrongKey(String container) {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        try {

            ReadStorePassword origReadStorePassword = new ReadStorePassword("affe1");
            ReadKeyPassword origReadKeyPassword = new ReadKeyPassword("password2");
            String keyStoreID = "keystore";
            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff = new KeyStoreServiceTest(extendedStoreConnection).createKeyStore(container, origReadStorePassword, origReadKeyPassword, keyStoreID, null);
            DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest(extendedStoreConnection);

            DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuff = documentGuardServiceTest.testCreateSymmetricDocumentGuard(
                    keyStoreStuff.keyStoreAccess, AccessType.WRITE);
            BucketPath keyStorePath = keyStoreStuff.keyStoreAccess.getKeyStorePath();

            ReadKeyPassword readKeyPassword = keyStoreStuff.keyStoreAccess.getKeyStoreAuth().getReadKeyPassword();
            ReadStorePassword readStorePassword = keyStoreStuff.keyStoreAccess.getKeyStoreAuth().getReadStorePassword();

            {
                boolean waitForException = false;
                try {
                    ReadKeyPassword wrongReadKeyPassword = new ReadKeyPassword("hassenichtgesehen");
                    KeyStoreAuth newKeyStoreAuth = new KeyStoreAuth(readStorePassword, wrongReadKeyPassword);
                    KeyStoreAccess newKeyStoreAccess = new KeyStoreAccess(keyStorePath, newKeyStoreAuth);
                    documentGuardServiceTest.testLoadDocumentGuard(newKeyStoreAccess,
                            documentGuardStuff.documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKeyID());
                } catch (Exception e) {
                    if (e.getCause() instanceof UnrecoverableKeyException) {
                        waitForException = true;
                    }
                }
                Assert.assertTrue("UnrecoverableKeyException for wrong keyPassword did not raise", waitForException);
                LOGGER.error("Exception was expected, thats fine");
            }
            int returnValue = 0;
            {
                boolean waitForException = false;
                try {
                    ReadStorePassword wrongReadStorePassword = new ReadStorePassword("hassenichtgesehen");
                    KeyStoreAuth newKeyStoreAuth = new KeyStoreAuth(wrongReadStorePassword, readKeyPassword);
                    KeyStoreAccess newKeyStoreAccess = new KeyStoreAccess(keyStorePath, newKeyStoreAuth);
                    documentGuardServiceTest.testLoadDocumentGuard(newKeyStoreAccess,
                            documentGuardStuff.documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKeyID());
                } catch (Exception e) {
                    if (e.getCause() instanceof UTFDataFormatException) {
                        waitForException = true;
                        returnValue = 1;
                    } else if (e.getCause() instanceof IOException) {
                        waitForException = true;
                        returnValue = 2;
                    }
                }
                Assert.assertTrue("UTFDataFormatException for wrong storePassword did not raise", waitForException);
                LOGGER.error("Exception was expected, thats fine");
            }
            KeyStoreAuth newKeyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);
            KeyStoreAccess newKeyStoreAccess = new KeyStoreAccess(keyStorePath, newKeyStoreAuth);
            DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = documentGuardServiceTest.testLoadDocumentGuard(newKeyStoreAccess,
                    documentGuardStuff.documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKeyID());

            LOGGER.debug("DocumentKey is " + HexUtil.convertBytesToHexString(documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKey().getSecretKey().getEncoded()));
            return returnValue;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }


    @Test
    public void testCreateDocument() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        try {
            DocumentContent documentContent = new DocumentContent("Ein Affe im Zoo ist nie allein".getBytes());

            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff = new KeyStoreServiceTest(extendedStoreConnection).createKeyStore();
            DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest(extendedStoreConnection);
            DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuff = documentGuardServiceTest.testCreateSymmetricDocumentGuard(
                    keyStoreStuff.keyStoreAccess, AccessType.WRITE);
            DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = documentGuardServiceTest.testLoadDocumentGuard(
                    keyStoreStuff.keyStoreAccess,
                    documentGuardStuff.documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKeyID());
            new DocumentPersistenceServiceTest(extendedStoreConnection).testPersistDocument(
                    documentGuardStuff.documentGuardService,
                    new DocumentBucketPath("documentbucketpath1/doc1.txt"),
                    documentKeyIDWithKeyAndAccessType, documentContent);
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testCreateAndLoadDocument() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        try {
            DocumentContent documentContent = new DocumentContent("Ein Affe im Zoo ist nie allein".getBytes());

            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff = new KeyStoreServiceTest(extendedStoreConnection).createKeyStore();
            DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest(extendedStoreConnection);
            DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuff = documentGuardServiceTest.testCreateSymmetricDocumentGuard(
                    keyStoreStuff.keyStoreAccess, AccessType.WRITE);
            DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = documentGuardServiceTest.testLoadDocumentGuard(
                    keyStoreStuff.keyStoreAccess,
                    documentGuardStuff.documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKeyID());
            DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest(extendedStoreConnection);
            DocumentPersistenceServiceTest.DocumentStuff documentStuff = new DocumentPersistenceServiceTest(extendedStoreConnection).testPersistDocument(
                    documentGuardStuff.documentGuardService,
                    new DocumentBucketPath("documentbucketpath2/doc2.txt"),
                    documentKeyIDWithKeyAndAccessType,
                    documentContent);
            Payload payload = documentPersistenceServiceTest.testLoadDocument(documentGuardStuff.documentGuardService,
                    keyStoreStuff.keyStoreAccess,
                    documentStuff.documentBucketPath);

            Assert.assertEquals("Content of Document", new DocumentContent(payload.getData()).toString(), documentContent.toString());

            LOGGER.debug("DocumentBucketPath         :" + documentStuff.documentBucketPath);
            LOGGER.debug("DocumentKeyIDAndAccessType :" + documentKeyIDWithKeyAndAccessType);
            LOGGER.debug("KeyStorePath               :" + keyStoreStuff.keyStoreAccess.getKeyStorePath());
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }


    /**
     * Es wird ein KeyStore angelegt, der nur einen symmetrischen Key enthält.
     * Ein Document wird angelegt, es wird mit einem neuen DocumentKey verschlüsselt.
     * Dieser wird mit dem symmetrischen Key des KeyStores in einem DocumentGuard abgelegt.
     * <p>
     * Dann wird ein zweiter KeyStore angelegt, der nur ein Private/Public-Keypain enthält.
     * Es wird ein DocumentGuard für den oben erzeugten DocumentKey erzeugt, aber nur
     * mit dem PublicKey. Es gibt keinen Zugriff auf ReadKeyPassword.
     * <p>
     * Das Document wird nun mit dem Privaten Key gelesen.
     */
    @Test
    public void testCreate_oneDocument_twoKeyStores_twoGuards_LoadDocument() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        String container1 = "user1/key-store-container-for-secretkey";
        String container2 = "user2/key-store-container-for-enckey";
        DocumentBucketPath documentBucketPath = new DocumentBucketPath("documentbucketpath3/subfolder/1/2/3");
        DocumentContent documentContent = new DocumentContent("Ein Affe im Zoo ist nie allein".getBytes());

        try {
            FullStuff symmetricStuff = createKeyStoreAndDocument(container1, documentBucketPath, documentContent);
            createPublicKeyStoreForKnownDocument(container2, documentContent, symmetricStuff.documentStuff.documentBucketPath, symmetricStuff.documentGuardStuff.documentKeyIDWithKeyAndAccessType, true);
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    /**
     * wie der vorige Test, nur dass der SecretKey nicht gelesen werden können soll,
     * weil er nicht bekannt ist. Daher expected Excption
     */
    @Test(expected = BaseException.class)
    public void testCreate_oneDocument_twoKeyStores_twoGuards_LoadDocument_with_expected_failure() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        String container1 = "user1/.keystore/key-store-container-for-secretkey";
        String container2 = "user2/.keystore/key-store-container-for-enckey";
        DocumentBucketPath documentBucketPath = new DocumentBucketPath("documentBucketPath/4");

        DocumentContent documentContent = new DocumentContent("Ein Affe im Zoo ist nie allein".getBytes());

        try {
            FullStuff symmetricStuff = createKeyStoreAndDocument(container1, documentBucketPath, documentContent);
            createPublicKeyStoreForKnownDocument(container2, documentContent, symmetricStuff.documentStuff.documentBucketPath, symmetricStuff.documentGuardStuff.documentKeyIDWithKeyAndAccessType, false);
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testCreate_oneDocument_twoKeyStores_twoGuards_ChangeDocument() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        String container1 = "user1/key-store-container-for-secretkey";
        String container2 = "user2/key-store-container-for-enckey";
        DocumentBucketPath documentBucketPath = new DocumentBucketPath("documentbucketpath5/1/2/3");

        DocumentContent documentContent = new DocumentContent("Ein Affe im Zoo ist nie allein".getBytes());
        try {
            FullStuff symmetricStuff = createKeyStoreAndDocument(container1, documentBucketPath, documentContent);
            FullStuff asymmetricStuff = createPublicKeyStoreForKnownDocument(container2, documentContent, symmetricStuff.documentStuff.documentBucketPath, symmetricStuff.documentGuardStuff.documentKeyIDWithKeyAndAccessType, true);

            // Neuer Inhalt für das Document, für das es bereits zwei Guards gibt
            DocumentContent newDocumentContent = new DocumentContent("ein anderer affe im zoo".getBytes());
            DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest(extendedStoreConnection);
            DocumentPersistenceServiceTest.DocumentStuff documentStuff = documentPersistenceServiceTest.testPersistDocument(
                    asymmetricStuff.documentGuardStuff.documentGuardService,
                    documentBucketPath,
                    asymmetricStuff.documentGuardStuff.documentKeyIDWithKeyAndAccessType,
                    newDocumentContent,
                    OverwriteFlag.TRUE
            );
            LOGGER.debug("Document erfolgreich ERNEUT geschrieben");

            // Load with asymmetric key
            Payload payload = documentPersistenceServiceTest.testLoadDocument(
                    asymmetricStuff.documentGuardStuff.documentGuardService,
                    asymmetricStuff.keyStoreStuff.keyStoreAccess,
                    documentStuff.documentBucketPath);
            Assert.assertEquals("Content of Document", newDocumentContent.toString(), new DocumentContent(payload.getData()).toString());
            LOGGER.debug("Document erfolgreich mit DocumentGuard für EncKey gelesen");

        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    @Test(expected = FileExistsException.class)
    public void testCreateDocumentTwice() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        DocumentBucketPath documentBucketPath = new DocumentBucketPath("user1/bucket/folder1");
        DocumentContent documentContent = new DocumentContent("Affe".getBytes());

        DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest(extendedStoreConnection);
        DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = new DocumentKeyIDWithKeyAndAccessType(documentGuardServiceTest.createKeyIDWithKey(), AccessType.WRITE);

        DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest(extendedStoreConnection);
        DocumentBucketPath netDocumentBucketPath = new DocumentBucketPath(documentBucketPath.append("AffenDocument-1"));
        documentPersistenceServiceTest.testPersistDocument(null, netDocumentBucketPath, documentKeyIDWithKeyAndAccessType, documentContent, OverwriteFlag.FALSE);
        documentPersistenceServiceTest.testPersistDocument(null, netDocumentBucketPath, documentKeyIDWithKeyAndAccessType, documentContent, OverwriteFlag.FALSE);
    }

    @Test
    public void testCreateDocumentTwiceButOverwrite() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        DocumentBucketPath documentBucketPath = new DocumentBucketPath("user1/bucket/folder1");
        DocumentContent documentContent = new DocumentContent("Affe".getBytes());

        DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest(extendedStoreConnection);
        DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = new DocumentKeyIDWithKeyAndAccessType(documentGuardServiceTest.createKeyIDWithKey(), AccessType.WRITE);

        DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest(extendedStoreConnection);
        DocumentBucketPath netDocumentBucketPath = new DocumentBucketPath(documentBucketPath.append("AffenDocument-1"));
        documentPersistenceServiceTest.testPersistDocument(null, netDocumentBucketPath, documentKeyIDWithKeyAndAccessType, documentContent, OverwriteFlag.FALSE);
        documentPersistenceServiceTest.testPersistDocument(null, netDocumentBucketPath, documentKeyIDWithKeyAndAccessType, documentContent, OverwriteFlag.TRUE);

    }

    @Test
    public void testBucketService1() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketServiceTest bucketServiceTest = new BucketServiceTest(extendedStoreConnection);
        BucketDirectory rootDirectory = new BucketDirectory("user1");
        bucketServiceTest.createFiles(extendedStoreConnection, rootDirectory, 3, 2);

        BucketContent bucketContent1 = bucketServiceTest.listBucket(rootDirectory, ListRecursiveFlag.FALSE);
        LOGGER.debug("1 einfaches listing" + bucketContent1.toString());
        Assert.assertEquals("nicht rekursiv erwartete Einträge", 6, bucketContent1.getContent().size());

        BucketContent bucketContent2 = bucketServiceTest.listBucket(rootDirectory, ListRecursiveFlag.TRUE);
        LOGGER.debug("2 recursives listing " + bucketContent2.toString());
        List<BucketDirectory> dirs = getDirectoresOnly(bucketContent2.getContent());
        List<BucketPath> files = getFilesOnly(bucketContent2.getContent());
        Assert.assertEquals("number of entries", 39, bucketContent2.getContent().size());
        Assert.assertEquals("number of entries", 13, dirs.size());
        Assert.assertEquals("number of entries", 26, files.size());
    }

    private boolean contains(List<StorageMetadata> content, String file0) {
        for (StorageMetadata m : content) {
            if (m.getName().equals(file0)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void checkNonExistingBucket() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketServiceTest bucketServiceTest = new BucketServiceTest(extendedStoreConnection);
        BucketDirectory bucketDirectory = new BucketDirectory("user1");
        boolean exists = bucketServiceTest.bucketExists(bucketDirectory);
        Assert.assertFalse("bucket must not exist", exists);
    }

    @Test
    public void createBucketWithDot() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketServiceTest bucketServiceTest = new BucketServiceTest(extendedStoreConnection);

        DocumentBucketPath documentBucketPath = new DocumentBucketPath("user1/.hidden/Affenfile.txt");
        DocumentContent documentContent = new DocumentContent("Affe".getBytes());

        DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest(extendedStoreConnection);
        DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = new DocumentKeyIDWithKeyAndAccessType(documentGuardServiceTest.createKeyIDWithKey(), AccessType.WRITE);

        DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest(extendedStoreConnection);

        documentPersistenceServiceTest.testPersistDocument(null, documentBucketPath, documentKeyIDWithKeyAndAccessType, documentContent, OverwriteFlag.FALSE);

        BucketDirectory pathAsDirectory = new BucketDirectory(documentBucketPath);
        BucketContent bucketContent = bucketServiceTest.listBucket(pathAsDirectory, ListRecursiveFlag.FALSE.TRUE);
        LOGGER.debug(bucketContent.toString());
        Assert.assertEquals("this is no bucket, so no result expected", 0, bucketContent.getContent().size());
        boolean fileExsits = bucketServiceTest.fileExists(documentBucketPath);
        Assert.assertEquals("file should exist", true, fileExsits);

    }

    private void showBucketContent(BucketContent bucketContent2) {
        for (StorageMetadata meta : bucketContent2.getContent()) {
            LOGGER.debug("name: " + meta.getName());
            LOGGER.debug("size: " + meta.getSize());
            LOGGER.debug("type: " + meta.getType());
            LOGGER.debug("etag: " + meta.getETag());
            LOGGER.debug("providerid: " + meta.getProviderID());
            LOGGER.debug("creation: " + meta.getCreationDate());
            LOGGER.debug("uri: " + meta.getUri());
            LOGGER.debug(" ");
        }
    }

    private static class FullStuff {
        public final KeyStoreServiceTest.KeyStoreStuff keyStoreStuff;
        public final DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuff;
        public final DocumentPersistenceServiceTest.DocumentStuff documentStuff;

        public FullStuff(KeyStoreServiceTest.KeyStoreStuff keyStoreStuff, DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuff, DocumentPersistenceServiceTest.DocumentStuff documentStuff) {
            this.keyStoreStuff = keyStoreStuff;
            this.documentGuardStuff = documentGuardStuff;
            this.documentStuff = documentStuff;
        }
    }

    private FullStuff createKeyStoreAndDocument(String container1, DocumentBucketPath documentBucketPath, DocumentContent documentContent) {
        // Erzeugen eines ersten KeyStores nur mit SecretKey
        KeyStoreServiceTest.KeyStoreStuff keyStoreStuffForKeyStoreWithSecretKey = new KeyStoreServiceTest(extendedStoreConnection).createKeyStore(container1,
                new ReadStorePassword("a"),
                new ReadKeyPassword("b"),
                "first",
                new KeyStoreCreationConfigImpl(0, 0, 1));
        LOGGER.debug(ShowKeyStore.toString(keyStoreStuffForKeyStoreWithSecretKey.keyStore, keyStoreStuffForKeyStoreWithSecretKey.keyStoreAccess.getKeyStoreAuth().getReadKeyPassword()));
        LOGGER.debug("Ersten KeyStore mit SecretKey erfolgreich angelegt");

        // Erzeugen des ersten DocumentGuards mit dem dem KeyStore, der den Secret Key enthält
        DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest(extendedStoreConnection);
        DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuffForSecretKey = documentGuardServiceTest.testCreateSymmetricDocumentGuard(
                keyStoreStuffForKeyStoreWithSecretKey.keyStoreAccess, AccessType.WRITE);
        LOGGER.debug("Ersten DocumentGuard mit secretKey verschlüsselt");

        // Erzeugen des Documents mit dem DocumentGuard, der mit dem secretKey verschlüsselt ist
        DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest(extendedStoreConnection);
        DocumentPersistenceServiceTest.DocumentStuff documentStuff = documentPersistenceServiceTest.testPersistDocument(
                documentGuardStuffForSecretKey.documentGuardService,
                documentBucketPath,
                documentGuardStuffForSecretKey.documentKeyIDWithKeyAndAccessType,
                documentContent);
        LOGGER.debug("Document mit Schlüssel aus DocumentGuard verschlüsselt");

        // Laden des Documents mit dem KeyStore mit secretKey
        Payload payload = documentPersistenceServiceTest.testLoadDocument(documentGuardStuffForSecretKey.documentGuardService,
                keyStoreStuffForKeyStoreWithSecretKey.keyStoreAccess,
                documentStuff.documentBucketPath);
        Assert.assertEquals("Content of Document", new DocumentContent(payload.getData()).toString(), documentContent.toString());

        LOGGER.debug("Document mit DocumentGuard erfolgreich gelesen");

        return new FullStuff(keyStoreStuffForKeyStoreWithSecretKey, documentGuardStuffForSecretKey, documentStuff);
    }

    private FullStuff createPublicKeyStoreForKnownDocument(String container2, DocumentContent documentContent, DocumentBucketPath documentBucketPath, DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType, boolean setReadKeyPassword) {
        // Anlegen des zweiten KeyStores. Nur mit einen EncKey
        KeyStoreServiceTest.KeyStoreStuff keyStoreStuffForKeyStoreWithEncKey = new KeyStoreServiceTest(extendedStoreConnection).createKeyStore(container2,
                new ReadStorePassword("c"),
                new ReadKeyPassword("d"),
                "second",
                new KeyStoreCreationConfigImpl(1, 0, 0));
        LOGGER.debug(ShowKeyStore.toString(keyStoreStuffForKeyStoreWithEncKey.keyStore, keyStoreStuffForKeyStoreWithEncKey.keyStoreAccess.getKeyStoreAuth().getReadKeyPassword()));
        LOGGER.debug("Zweiten KeyStore mit EncKey erfolgreich angelegt");

        // vorher sicherheitshalber Löschen des Kennworts zum Lesen der Keys
        KeyStoreAccess keystoreAccessForKeyStoreWithEncKey = keyStoreStuffForKeyStoreWithEncKey.keyStoreAccess;
        ReadKeyPassword readKeyPassword = keystoreAccessForKeyStoreWithEncKey.getKeyStoreAuth().getReadKeyPassword();
        keystoreAccessForKeyStoreWithEncKey.getKeyStoreAuth().setReadKeyPassword(null);

        // Jetzt Erzeugen des zweiten DocumentGuards mit dem KeyStore, der den EncKey enthält
        DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest(extendedStoreConnection);
        DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuffForEncKey = documentGuardServiceTest.testCreateAsymmetricDocumentGuardForDocumentKeyIDWithKey(
                keystoreAccessForKeyStoreWithEncKey,
                documentKeyIDWithKeyAndAccessType);
        LOGGER.debug("Zweiten DocumentGuard mit EncKey ohne Wissen über das Kennwort des Keys angelegt");

        if (setReadKeyPassword) {
            // Jetzt Laden des Documents mit dem KeyStore, der mit dem EncKey verschlüsselt ist
            // Dazu muss der PrivateKey gelesen werden. Dazu muss das Kennwort zum Lesen der Keys wieder gesetzt sein
            keystoreAccessForKeyStoreWithEncKey.getKeyStoreAuth().setReadKeyPassword(readKeyPassword);
        } else {
            // Um den Fehlerfall testen zu können, wird hier ein ReadKeyPassword gesetzt, ansonsten
            // scheitert der Tests schon beim Zugriff auf das Kennwort. Wir wollen aber ganz sicher sein, dass der Fehler
            // beim eigentlichen Zugriff auf den private Key scheitert.
            keystoreAccessForKeyStoreWithEncKey.getKeyStoreAuth().setReadKeyPassword(new ReadKeyPassword(""));
        }

        // Load with asymmetric key
        DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest(extendedStoreConnection);
        Payload payload = documentPersistenceServiceTest.testLoadDocument(documentGuardStuffForEncKey.documentGuardService,
                keystoreAccessForKeyStoreWithEncKey,
                documentBucketPath);
        Assert.assertEquals("Content of Document", new DocumentContent(payload.getData()).toString(), documentContent.toString());
        LOGGER.debug("Document erfolgreich mit DocumentGuard für EncKey gelesen");

        return new FullStuff(keyStoreStuffForKeyStoreWithEncKey, documentGuardStuffForEncKey, null);

    }

    private List<BucketPath> getFilesOnly(List<StorageMetadata> a) {
        List<BucketPath> result = new ArrayList<>();
        for (StorageMetadata s : a) {
            if (s.getType().equals(StorageType.BLOB)) {
                result.add(new BucketPath(s.getName()));
            }

        }
        return result;
    }


    private List<BucketDirectory> getDirectoresOnly(List<StorageMetadata> a) {
        List<BucketDirectory> result = new ArrayList<>();
        for (StorageMetadata s : a) {
            if (s.getType().equals(StorageType.FOLDER)) {
                result.add(new BucketDirectory(s.getName()));
            }
        }
        return result;
    }
}