package org.adorsys.documentsafe.layer02service;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.utils.HexUtil;
import org.adorsys.documentsafe.layer02service.exceptions.KeyStoreExistsException;
import org.adorsys.documentsafe.layer02service.generators.KeyStoreCreationConfig;
import org.adorsys.documentsafe.layer02service.impl.ExtendedStorageMetadata;
import org.adorsys.documentsafe.layer02service.types.DocumentContent;
import org.adorsys.documentsafe.layer02service.types.ReadKeyPassword;
import org.adorsys.documentsafe.layer02service.types.ReadStorePassword;
import org.adorsys.documentsafe.layer02service.types.complextypes.BucketContent;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentBucketPath;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentContentWithContentMetaInfo;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKeyAndAccessType;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.documentsafe.layer02service.utils.ShowKeyStore;
import org.adorsys.documentsafe.layer02service.utils.TestKeyUtils;
import org.adorsys.documentsafe.layer03business.types.AccessType;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.exceptions.FileExistsException;
import org.adorsys.encobject.filesystem.FileSystemExtendedStorageConnection;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.service.ExtendedStoreConnection;
import org.adorsys.encobject.types.KeyStoreID;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.adorsys.encobject.types.OverwriteFlag;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by peter on 04.01.18.
 */
public class AllServiceTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(AllServiceTest.class);
    private final static ExtendedStoreConnection extendedStoreConnection = new FileSystemExtendedStorageConnection();
    public static Set<BucketPath> buckets = new HashSet<>();

    @Before
    public void before() {
        TestKeyUtils.turnOffEncPolicy();
        buckets.clear();
    }

    @After
    public void after() {
        try {
            ContainerPersistence containerPersistence = new ContainerPersistence(extendedStoreConnection);
            for (BucketPath bucket : buckets) {
                String container = bucket.getObjectHandle().getContainer();
                LOGGER.debug("AFTER TEST: DELETE BUCKET " + container);
                containerPersistence.deleteContainer(container);
            }
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }

    @Test
    public void testCreateBucketPath() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        try {
            BucketPath bp = new BucketPath("1/2/3");
            ContainerPersistence containerPersistence = new ContainerPersistence(extendedStoreConnection);
            containerPersistence.creteContainer(bp.getObjectHandle().getContainer());
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

            LOGGER.debug("DocumentKey is " + HexUtil.conventBytesToHexString(documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKey().getSecretKey().getEncoded()));
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
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
                    new DocumentBucketPath("documentBucketPath1/doc1.txt"),
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
                    new DocumentBucketPath("documentBucketPath2/doc2.txt"),
                    documentKeyIDWithKeyAndAccessType,
                    documentContent);
            DocumentContentWithContentMetaInfo readContent = documentPersistenceServiceTest.testLoadDocument(documentGuardStuff.documentGuardService,
                    keyStoreStuff.keyStoreAccess,
                    documentStuff.documentBucketPath);

            Assert.assertEquals("Content of Document", readContent.getDocumentContent().toString(), documentContent.toString());

            LOGGER.debug("DocumentBucketPath         :" + documentStuff.documentBucketPath);
            LOGGER.debug("DocumentKeyIDAndAccessType :" + documentKeyIDWithKeyAndAccessType);
            LOGGER.debug("KeyStoreLocation           :" + keyStoreStuff.keyStoreAccess.getKeyStoreLocation());
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
        DocumentBucketPath documentBucketPath = new DocumentBucketPath("documentBucketPath3/subfolder/1/2/3");
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
        DocumentBucketPath documentBucketPath = new DocumentBucketPath("documentBucketPath5/1/2/3");

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
            DocumentContentWithContentMetaInfo newReadDocumentContent = documentPersistenceServiceTest.testLoadDocument(
                    asymmetricStuff.documentGuardStuff.documentGuardService,
                    asymmetricStuff.keyStoreStuff.keyStoreAccess,
                    documentStuff.documentBucketPath);
            Assert.assertEquals("Content of Document", newDocumentContent.toString(), newReadDocumentContent.getDocumentContent().toString());
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
        BucketPath rootPath = new BucketPath("user1");
        bucketServiceTest.createFiles(extendedStoreConnection, rootPath, 3,2);

        BucketContent bucketContent1 = bucketServiceTest.listBucket(rootPath, ListRecursiveFlag.FALSE);
        LOGGER.debug("1 einfaches listing" + bucketContent1.toString());
        Assert.assertEquals("nicht rekursiv erwartete Einträge", 5, bucketContent1.getStrippedContent().size());

        BucketContent bucketContent2 = bucketServiceTest.listBucket(rootPath, ListRecursiveFlag.TRUE);
        LOGGER.debug("2 recursives listing " + bucketContent2.toString());
        Assert.assertEquals("rekursiv erwartete Einträge", 26, bucketContent2.getStrippedContent().size());

        BucketPath bp = rootPath.append("subdir1");
        BucketContent bucketContent3 = bucketServiceTest.listBucket(bp, ListRecursiveFlag.FALSE);
        LOGGER.debug("3 einfaches listing " + bucketContent3.toString());
        Assert.assertEquals("rekursiv erwartete Einträge", 5, bucketContent3.getStrippedContent().size());
        Assert.assertTrue("es gibt file", contains(bucketContent3.getStrippedContent(), "file0"));
        Assert.assertTrue("es gibt directory", contains(bucketContent3.getStrippedContent(), "subdir0/"));

        BucketContent bucketContent4 = bucketServiceTest.listBucket(bp, ListRecursiveFlag.TRUE);
        LOGGER.debug("4 recursives listing " + bucketContent4.toString());
        Assert.assertEquals("rekursiv erwartete Einträge", 8, bucketContent4.getStrippedContent().size());
        Assert.assertTrue("es gibt", contains(bucketContent4.getStrippedContent(), "file0"));
        Assert.assertTrue("es gibt directory", contains(bucketContent4.getStrippedContent(), "subdir0/file0"));
    }

    private boolean contains(List<ExtendedStorageMetadata> strippedContent, String file0) {
        for (ExtendedStorageMetadata m : strippedContent) {
            if (m.getName().equals(file0)){
                return true;
            }
        }
        return false;
    }

    @Test
    public void checkNonExistingBucket() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketServiceTest bucketServiceTest = new BucketServiceTest(extendedStoreConnection);
        BucketPath rootPath = new BucketPath("user1");
        boolean exists = bucketServiceTest.bucketExists(rootPath);
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

        BucketContent bucketContent = bucketServiceTest.listBucket(documentBucketPath, ListRecursiveFlag.FALSE.TRUE);
        Assert.assertEquals("this is no bucket, so no result expected", 0, bucketContent.getStrippedContent().size());
        boolean fileExsits = bucketServiceTest.bucketExists(documentBucketPath);
        Assert.assertEquals("file should exist", true, fileExsits);

    }

    private void showBucketContent(BucketContent bucketContent2) {
        for (StorageMetadata meta : bucketContent2.getStrippedContent()) {
            LOGGER.debug("name: " + meta.getName());
            LOGGER.debug("size: " + meta.getSize());
            LOGGER.debug("type: " + meta.getType());
            LOGGER.debug("etag: " + meta.getETag());
            LOGGER.debug("providerid: " + meta.getProviderId());
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
                new KeyStoreID("first"),
                new KeyStoreCreationConfig(0, 0, 1));
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
        DocumentContentWithContentMetaInfo readDocumentContent = documentPersistenceServiceTest.testLoadDocument(documentGuardStuffForSecretKey.documentGuardService,
                keyStoreStuffForKeyStoreWithSecretKey.keyStoreAccess,
                documentStuff.documentBucketPath);
        Assert.assertEquals("Content of Document", readDocumentContent.getDocumentContent().toString(), documentContent.toString());

        LOGGER.debug("Document mit DocumentGuard erfolgreich gelesen");

        return new FullStuff(keyStoreStuffForKeyStoreWithSecretKey, documentGuardStuffForSecretKey, documentStuff);
    }

    private FullStuff createPublicKeyStoreForKnownDocument(String container2, DocumentContent documentContent, DocumentBucketPath documentBucketPath, DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType, boolean setReadKeyPassword) {
        // Anlegen des zweiten KeyStores. Nur mit einen EncKey
        KeyStoreServiceTest.KeyStoreStuff keyStoreStuffForKeyStoreWithEncKey = new KeyStoreServiceTest(extendedStoreConnection).createKeyStore(container2,
                new ReadStorePassword("c"),
                new ReadKeyPassword("d"),
                new KeyStoreID("second"),
                new KeyStoreCreationConfig(1, 0, 0));
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
        DocumentContentWithContentMetaInfo readDocumentContent = documentPersistenceServiceTest.testLoadDocument(documentGuardStuffForEncKey.documentGuardService,
                keystoreAccessForKeyStoreWithEncKey,
                documentBucketPath);
        Assert.assertEquals("Content of Document", readDocumentContent.getDocumentContent().toString(), documentContent.toString());
        LOGGER.debug("Document erfolgreich mit DocumentGuard für EncKey gelesen");

        return new FullStuff(keyStoreStuffForKeyStoreWithEncKey, documentGuardStuffForEncKey, null);

    }

}
