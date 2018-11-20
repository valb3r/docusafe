package org.adorsys.docusafe.service;

import com.googlecode.catchexception.CatchException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.extendendstoreconnection.impl.amazons3.AmazonS3ExtendedStoreConnection;
import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.cryptoutils.utils.HexUtil;
import org.adorsys.docusafe.service.types.AccessType;
import org.adorsys.docusafe.service.types.BucketContent;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.docusafe.service.types.complextypes.BucketContentImpl;
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
import java.util.UUID;

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
        ContainerPersistence containerPersistence = new ContainerPersistenceImpl(extendedStoreConnection);
        for (BucketDirectory bucket : buckets) {
            try {
                LOGGER.debug("AFTER TEST: DELETE BUCKET " + bucket);
                containerPersistence.deleteContainer(bucket);
            } catch (Exception e) {
                // ignore Exception
            }
        }
    }

    @Test
    public void testCreateBucketPath() {
        
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
        
        try {
            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff = new KeyStoreServiceTest(extendedStoreConnection).createKeyStore();
            Assert.assertEquals("Number of Entries", 15, keyStoreStuff.keyStore.size());
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    @Test(expected = KeyStoreExistsException.class)
    public void testCreateKeyStoreTwice() {
        
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
            LOGGER.debug("TEST WRONG LOOP IGNROED. PLEASE RUN WITH -Dloop");
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
                        LOGGER.debug("========================================");
                        LOGGER.debug("FOUND EXCEPTION 1 WITH CONTAINERID " + i);
                        LOGGER.debug("========================================");
                        foundFirstException = true;
                    } else {
                        after();
                    }
                }
                if (e == 2) {
                    if (!foundSecondException) {
                        LOGGER.debug("========================================");
                        LOGGER.debug("FOUND EXCEPTION 2 WITH CONTAINERID " + i);
                        LOGGER.debug("========================================");
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
        
        String container1 = "user1/secretkey";
        String container2 = "user2/enckey";
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
    @Test
    public void testCreate_oneDocument_twoKeyStores_twoGuards_LoadDocument_with_expected_failure() {
        
        String container1 = "user1/secretkey";
        String container2 = "user2/enckey";
        DocumentBucketPath documentBucketPath = new DocumentBucketPath("documentbucketpath/4");

        DocumentContent documentContent = new DocumentContent("Ein Affe im Zoo ist nie allein".getBytes());

        FullStuff symmetricStuff = createKeyStoreAndDocument(container1, documentBucketPath, documentContent);
        CatchException.catchException(() -> createPublicKeyStoreForKnownDocument(container2, documentContent, symmetricStuff.documentStuff.documentBucketPath, symmetricStuff.documentGuardStuff.documentKeyIDWithKeyAndAccessType, false));
        Assert.assertTrue(CatchException.caughtException() != null);
    }

    // @Test
    public void cleanDB() {
        if (extendedStoreConnection instanceof AmazonS3ExtendedStoreConnection) {
            ((AmazonS3ExtendedStoreConnection) extendedStoreConnection).cleanDatabase();
        }
    }
    @Test
    public void testCreate_oneDocument_twoKeyStores_twoGuards_ChangeDocument() {
        
        String container1 = "user1/secretkey";
        String container2 = "user2/enckey";
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

    @Test
    public void checkNonExistingBucket() {
        
        BucketServiceTest bucketServiceTest = new BucketServiceTest(extendedStoreConnection);
        BucketDirectory bucketDirectory = new BucketDirectory("user-" + UUID.randomUUID().toString());
        boolean exists = bucketServiceTest.bucketExists(bucketDirectory);
        Assert.assertFalse("bucket must not exist", exists);
    }

    // @Test
    // TODO spezialtest, bei dem das directory auch als pfad benutzt wird, kann mit dsc-encryption-filename-only nicht gehen
    public void createBucketWithDot() {
        
        BucketServiceTest bucketServiceTest = new BucketServiceTest(extendedStoreConnection);

        DocumentBucketPath documentBucketPath = new DocumentBucketPath("user1/.hidden/Affenfile.txt");
        DocumentContent documentContent = new DocumentContent("Affe".getBytes());

        DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest(extendedStoreConnection);
        DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = new DocumentKeyIDWithKeyAndAccessType(documentGuardServiceTest.createKeyIDWithKey(), AccessType.WRITE);

        DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest(extendedStoreConnection);

        documentPersistenceServiceTest.testPersistDocument(null, documentBucketPath, documentKeyIDWithKeyAndAccessType, documentContent, OverwriteFlag.FALSE);

        BucketDirectory pathAsDirectory = new BucketDirectory(documentBucketPath);
        LOGGER.debug("bucketPath " + documentBucketPath);
        LOGGER.debug("pathAsDir  " + pathAsDirectory);
        BucketContent bucketContent = bucketServiceTest.listBucket(pathAsDirectory, ListRecursiveFlag.TRUE);
        LOGGER.debug(bucketContent.toString());
        Assert.assertTrue("this is no bucket, so no result expected", bucketContent.getFiles().isEmpty());
        Assert.assertTrue("this is no bucket, so no result expected", bucketContent.getSubdirectories().isEmpty());
        boolean fileExsits = bucketServiceTest.fileExists(documentBucketPath);
        Assert.assertEquals("file should exist", true, fileExsits);

    }


    @Test
    public void createManyBuckets() {
        int NUMBER_OF_BUCKETS = 200;
        // TODO actually a performancetest
        NUMBER_OF_BUCKETS = 2;
        BucketServiceTest bucketServiceTest = new BucketServiceTest(extendedStoreConnection);
        for (int i = 0; i < NUMBER_OF_BUCKETS; i++) {
            BucketDirectory bd = new BucketDirectory("bucket" + i);
            buckets.add(bd);
            bucketServiceTest.createBucket(bd);
        }
    }

    private void showBucketContent(BucketContentImpl bucketContent2) {
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
