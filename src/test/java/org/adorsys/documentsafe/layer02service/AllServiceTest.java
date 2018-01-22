package org.adorsys.documentsafe.layer02service;

import org.adorsys.documentsafe.layer00common.exceptions.BaseException;
import org.adorsys.documentsafe.layer00common.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer00common.utils.HexUtil;
import org.adorsys.documentsafe.layer01persistence.ExtendedBlobStoreConnection;
import org.adorsys.documentsafe.layer01persistence.exceptions.FileExistsException;
import org.adorsys.documentsafe.layer01persistence.types.BucketName;
import org.adorsys.documentsafe.layer01persistence.types.KeyStoreID;
import org.adorsys.documentsafe.layer01persistence.types.ListRecursiveFlag;
import org.adorsys.documentsafe.layer01persistence.types.OverwriteFlag;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.adorsys.documentsafe.layer02service.exceptions.KeyStoreExistsException;
import org.adorsys.documentsafe.layer02service.generators.KeyStoreCreationConfig;
import org.adorsys.documentsafe.layer02service.types.DocumentContent;
import org.adorsys.documentsafe.layer02service.types.DocumentID;
import org.adorsys.documentsafe.layer02service.types.ReadKeyPassword;
import org.adorsys.documentsafe.layer02service.types.ReadStorePassword;
import org.adorsys.documentsafe.layer02service.types.complextypes.BucketContent;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentBucketPath;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentLocation;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.documentsafe.layer02service.utils.ShowKeyStore;
import org.adorsys.encobject.service.BlobStoreConnection;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.documentsafe.layer02service.utils.TestFsBlobStoreFactory;
import org.adorsys.documentsafe.layer02service.utils.TestKeyUtils;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by peter on 04.01.18.
 */
public class AllServiceTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(AllServiceTest.class);
    private final static BlobStoreContextFactory factory = new TestFsBlobStoreFactory();
    public static Set<BucketPath> buckets = new HashSet<>();

    @Before
    public void before() {
        TestKeyUtils.turnOffEncPolicy();
        buckets.clear();
    }

    @After
    public void after() {
        try {
            ContainerPersistence containerPersistence = new ContainerPersistence(new ExtendedBlobStoreConnection(factory));
            for (BucketPath bucket : buckets) {
                LOGGER.debug("AFTER TEST: DELETE BUCKET " + bucket.getFirstBucket());
                containerPersistence.deleteContainer(bucket.getFirstBucket().getValue());
            }
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }

    @Test
    public void testCreateBucketPath() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        try {
            BucketPath bp = new BucketPath().set(new BucketName("1")).sub(new BucketName("2")).sub(new BucketName("3"));
            ContainerPersistence containerPersistence = new ContainerPersistence(new ExtendedBlobStoreConnection(factory));
            containerPersistence.creteContainer(bp.getObjectHandlePath());
            buckets.add(bp);
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testCreateKeyStore() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        try {
            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff = new KeyStoreServiceTest(factory).createKeyStore();
            Assert.assertEquals("Number of Entries", 15, keyStoreStuff.keyStore.size());
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    @Test(expected = KeyStoreExistsException.class)
    public void testCreateKeyStoreTwice() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        try {
            KeyStoreServiceTest keyStoreServiceTest = new KeyStoreServiceTest(factory);
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
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        try {
            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff = new KeyStoreServiceTest(factory).createKeyStore();
            new DocumentGuardServiceTest(factory).testCreateSymmetricDocumentGuard(
                    keyStoreStuff.keyStoreAccess);
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testCreateKeyStoreAndDocumentGuardAndLoadDocumentGuard() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        try {
            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff = new KeyStoreServiceTest(factory).createKeyStore();
            DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest(factory);
            DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuff = documentGuardServiceTest.testCreateSymmetricDocumentGuard(
                    keyStoreStuff.keyStoreAccess);
            DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardServiceTest.testLoadDocumentGuard(
                    keyStoreStuff.keyStoreAccess,
                    documentGuardStuff.documentKeyIDWithKey.getDocumentKeyID());

            LOGGER.debug("DocumentKey is " + HexUtil.conventBytesToHexString(documentKeyIDWithKey.getDocumentKey().getSecretKey().getEncoded()));
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testCreateDocument() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        try {
            DocumentContent documentContent = new DocumentContent("Ein Affe im Zoo ist nie allein".getBytes());

            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff = new KeyStoreServiceTest(factory).createKeyStore();
            DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest(factory);
            DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuff = documentGuardServiceTest.testCreateSymmetricDocumentGuard(
                    keyStoreStuff.keyStoreAccess);
            DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardServiceTest.testLoadDocumentGuard(
                    keyStoreStuff.keyStoreAccess,
                    documentGuardStuff.documentKeyIDWithKey.getDocumentKeyID());
            new DocumentPersistenceServiceTest(factory).testPersistDocument(
                    documentGuardStuff.documentGuardService,
                    new DocumentBucketPath("documentBucketPath1"),
                    documentKeyIDWithKey, documentContent);
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testCreateAndLoadDocument() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        try {
            DocumentContent documentContent = new DocumentContent("Ein Affe im Zoo ist nie allein".getBytes());

            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff = new KeyStoreServiceTest(factory).createKeyStore();
            DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest(factory);
            DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuff = documentGuardServiceTest.testCreateSymmetricDocumentGuard(
                    keyStoreStuff.keyStoreAccess);
            DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardServiceTest.testLoadDocumentGuard(
                    keyStoreStuff.keyStoreAccess,
                    documentGuardStuff.documentKeyIDWithKey.getDocumentKeyID());
            DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest(factory);
            DocumentPersistenceServiceTest.DocumentStuff documentStuff = new DocumentPersistenceServiceTest(factory).testPersistDocument(
                    documentGuardStuff.documentGuardService,
                    new DocumentBucketPath("documentBucketPath2"),
                    documentKeyIDWithKey,
                    documentContent);
            DocumentContent readContent = documentPersistenceServiceTest.testLoadDocument(documentGuardStuff.documentGuardService,
                    keyStoreStuff.keyStoreAccess,
                    documentStuff.documentLocation);

            Assert.assertEquals("Content of Document", readContent.toString(), documentContent.toString());

            LOGGER.debug("DocumentLocation     :" + documentStuff.documentLocation);
            LOGGER.debug("DocumentKeyID        :" + documentKeyIDWithKey.getDocumentKeyID());
            LOGGER.debug("KeyStoreLocation     :" + keyStoreStuff.keyStoreAccess.getKeyStoreLocation());
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
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        String container1 = "user1/key-store-container-for-secretkey";
        String container2 = "user2/key-store-container-for-enckey";
        DocumentBucketPath documentBucketPath = new DocumentBucketPath("documentBucketPath3/subfolder/1/2/3");
        DocumentContent documentContent = new DocumentContent("Ein Affe im Zoo ist nie allein".getBytes());

        try {
            FullStuff symmetricStuff = createKeyStoreAndDocument(container1, documentBucketPath, documentContent);
            createPublicKeyStoreForKnownDocument(container2, documentContent, symmetricStuff.documentStuff.documentLocation, symmetricStuff.documentGuardStuff.documentKeyIDWithKey, true);
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
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        String container1 = "user1/.keystore/key-store-container-for-secretkey";
        String container2 = "user2/.keystore/key-store-container-for-enckey";
        DocumentBucketPath documentBucketPath = new DocumentBucketPath("documentBucketPath/4");

        DocumentContent documentContent = new DocumentContent("Ein Affe im Zoo ist nie allein".getBytes());

        try {
            FullStuff symmetricStuff = createKeyStoreAndDocument(container1, documentBucketPath, documentContent);
            createPublicKeyStoreForKnownDocument(container2, documentContent, symmetricStuff.documentStuff.documentLocation, symmetricStuff.documentGuardStuff.documentKeyIDWithKey, false);
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testCreate_oneDocument_twoKeyStores_twoGuards_ChangeDocument() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        String container1 = "user1/key-store-container-for-secretkey";
        String container2 = "user2/key-store-container-for-enckey";
        DocumentBucketPath documentBucketPath = new DocumentBucketPath("documentBucketPath5/1/2/3");

        DocumentContent documentContent = new DocumentContent("Ein Affe im Zoo ist nie allein".getBytes());
        try {
            FullStuff symmetricStuff = createKeyStoreAndDocument(container1, documentBucketPath, documentContent);
            FullStuff asymmetricStuff = createPublicKeyStoreForKnownDocument(container2, documentContent, symmetricStuff.documentStuff.documentLocation, symmetricStuff.documentGuardStuff.documentKeyIDWithKey, true);

            // Neuer Inhalt für das Document, für das es bereits zwei Guards gibt
            DocumentContent newDocumentContent = new DocumentContent("ein anderer affe im zoo".getBytes());
            DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest(factory);
            DocumentPersistenceServiceTest.DocumentStuff documentStuff = documentPersistenceServiceTest.testPersistDocument(
                    asymmetricStuff.documentGuardStuff.documentGuardService,
                    documentBucketPath,
                    asymmetricStuff.documentGuardStuff.documentKeyIDWithKey,
                    symmetricStuff.documentStuff.documentID,
                    newDocumentContent,
                    OverwriteFlag.TRUE
            );
            LOGGER.debug("Document erfolgreich ERNEUT geschrieben");

            // Load with asymmetric key
            DocumentContent newReadDocumentContent = documentPersistenceServiceTest.testLoadDocument(
                    asymmetricStuff.documentGuardStuff.documentGuardService,
                    asymmetricStuff.keyStoreStuff.keyStoreAccess,
                    documentStuff.documentLocation);
            Assert.assertEquals("Content of Document", newDocumentContent.toString(), newReadDocumentContent.toString());
            LOGGER.debug("Document erfolgreich mit DocumentGuard für EncKey gelesen");

        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    @Test(expected = FileExistsException.class)
    public void testCreateDocumentTwice() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        DocumentBucketPath documentBucketPath = new DocumentBucketPath("user1/bucket/folder1");
        DocumentContent documentContent = new DocumentContent("Affe".getBytes());

        DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest(factory);
        DocumentKeyIDWithKey keyIDWithKey = documentGuardServiceTest.createKeyIDWithKey();

        DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest(factory);
        DocumentID documentID = new DocumentID("AffenDocument-1");
        documentPersistenceServiceTest.testPersistDocument(null, documentBucketPath, keyIDWithKey, documentID, documentContent, OverwriteFlag.FALSE);
        documentPersistenceServiceTest.testPersistDocument(null, documentBucketPath, keyIDWithKey, documentID, documentContent, OverwriteFlag.FALSE);
    }

    @Test
    public void testCreateDocumentTwiceButOverwrite() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        DocumentBucketPath documentBucketPath = new DocumentBucketPath("user1/bucket/folder1");
        DocumentContent documentContent = new DocumentContent("Affe".getBytes());

        DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest(factory);
        DocumentKeyIDWithKey keyIDWithKey = documentGuardServiceTest.createKeyIDWithKey();

        DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest(factory);
        DocumentID documentID = new DocumentID("AffenDocument-1");
        documentPersistenceServiceTest.testPersistDocument(null, documentBucketPath, keyIDWithKey, documentID, documentContent, OverwriteFlag.FALSE);
        documentPersistenceServiceTest.testPersistDocument(null, documentBucketPath, keyIDWithKey, documentID, documentContent, OverwriteFlag.TRUE);

    }

    @Test
    public void testBucketService1() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketServiceTest bucketServiceTest = new BucketServiceTest(factory);
        BucketPath rootPath = new BucketPath("user1");
        bucketServiceTest.createFiles(factory, rootPath, 3,2);

        BucketContent bucketContent1 = bucketServiceTest.listBucket(rootPath, ListRecursiveFlag.FALSE);
        LOGGER.debug("einfaches listing" + bucketContent1.toString());
        Assert.assertEquals("nicht rekursiv erwartete Einträge", 6, bucketContent1.getStrippedContent().size());

        BucketContent bucketContent2 = bucketServiceTest.listBucket(rootPath, ListRecursiveFlag.TRUE);
        LOGGER.debug("recursives listing " + bucketContent2.toString());
        Assert.assertEquals("rekursiv erwartete Einträge", 24, bucketContent2.getStrippedContent().size());

        BucketPath bp = new BucketPath(rootPath.getObjectHandlePath());
        bp.sub(new BucketName("bucket"));
        bp.sub(new BucketName("subbucket"));
        BucketContent bucketContent3 = bucketServiceTest.listBucket(bp, ListRecursiveFlag.FALSE);
        LOGGER.debug("einfaches listing " + bucketContent3.toString());
        Assert.assertEquals("rekursiv erwartete Einträge", 5, bucketContent3.getStrippedContent().size());

        BucketContent bucketContent4 = bucketServiceTest.listBucket(bp, ListRecursiveFlag.TRUE);
        LOGGER.debug("recursives listing " + bucketContent4.toString());
        Assert.assertEquals("rekursiv erwartete Einträge", 8, bucketContent4.getStrippedContent().size());

        // showBucketContent(bucketContent2);

    }

    @Test
    public void checkNonExistingBucket() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketServiceTest bucketServiceTest = new BucketServiceTest(factory);
        BucketPath rootPath = new BucketPath("user1");
        boolean exists = bucketServiceTest.bucketExists(rootPath);
        Assert.assertFalse("bucket must not exist", exists);
    }

    @Test
    public void createBucketWithDot() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        BucketServiceTest bucketServiceTest = new BucketServiceTest(factory);

        DocumentBucketPath documentBucketPath = new DocumentBucketPath("user1/.hidden");
        DocumentContent documentContent = new DocumentContent("Affe".getBytes());
        DocumentID documentID = new DocumentID("Affenfile.txt");

        BlobStoreConnection blobStoreConnection = new ExtendedBlobStoreConnection(new TestFsBlobStoreFactory());
        DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest(factory);
        DocumentKeyIDWithKey keyIDWithKey = documentGuardServiceTest.createKeyIDWithKey();
        DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest(factory);

        documentPersistenceServiceTest.testPersistDocument(null, documentBucketPath, keyIDWithKey, documentID, documentContent, OverwriteFlag.FALSE);

        BucketContent bucketContent = bucketServiceTest.listBucket(documentBucketPath, ListRecursiveFlag.FALSE.TRUE);
        Assert.assertEquals("only one file expected", 1, bucketContent.getStrippedContent().size());

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
        KeyStoreServiceTest.KeyStoreStuff keyStoreStuffForKeyStoreWithSecretKey = new KeyStoreServiceTest(factory).createKeyStore(container1,
                new ReadStorePassword("a"),
                new ReadKeyPassword("b"),
                new KeyStoreID("first"),
                new KeyStoreCreationConfig(0, 0, 1));
        LOGGER.debug(ShowKeyStore.toString(keyStoreStuffForKeyStoreWithSecretKey.keyStore, keyStoreStuffForKeyStoreWithSecretKey.keyStoreAccess.getKeyStoreAuth().getReadKeyPassword()));
        LOGGER.debug("Ersten KeyStore mit SecretKey erfolgreich angelegt");

        // Erzeugen des ersten DocumentGuards mit dem dem KeyStore, der den Secret Key enthält
        DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest(factory);
        DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuffForSecretKey = documentGuardServiceTest.testCreateSymmetricDocumentGuard(
                keyStoreStuffForKeyStoreWithSecretKey.keyStoreAccess);
        LOGGER.debug("Ersten DocumentGuard mit secretKey verschlüsselt");

        // Erzeugen des Documents mit dem DocumentGuard, der mit dem secretKey verschlüsselt ist
        DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest(factory);
        DocumentPersistenceServiceTest.DocumentStuff documentStuff = documentPersistenceServiceTest.testPersistDocument(
                documentGuardStuffForSecretKey.documentGuardService,
                documentBucketPath,
                documentGuardStuffForSecretKey.documentKeyIDWithKey,
                documentContent);
        LOGGER.debug("Document mit Schlüssel aus DocumentGuard verschlüsselt");

        // Laden des Documents mit dem KeyStore mit secretKey
        DocumentContent readDocumentContent = documentPersistenceServiceTest.testLoadDocument(documentGuardStuffForSecretKey.documentGuardService,
                keyStoreStuffForKeyStoreWithSecretKey.keyStoreAccess,
                documentStuff.documentLocation);
        Assert.assertEquals("Content of Document", readDocumentContent.toString(), documentContent.toString());

        LOGGER.debug("Document mit DocumentGuard erfolgreich gelesen");

        return new FullStuff(keyStoreStuffForKeyStoreWithSecretKey, documentGuardStuffForSecretKey, documentStuff);
    }

    private FullStuff createPublicKeyStoreForKnownDocument(String container2, DocumentContent documentContent, DocumentLocation documentLocation, DocumentKeyIDWithKey documentKeyIDWithKey, boolean setReadKeyPassword) {
        // Anlegen des zweiten KeyStores. Nur mit einen EncKey
        KeyStoreServiceTest.KeyStoreStuff keyStoreStuffForKeyStoreWithEncKey = new KeyStoreServiceTest(factory).createKeyStore(container2,
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
        DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest(factory);
        DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuffForEncKey = documentGuardServiceTest.testCreateAsymmetricDocumentGuardForDocumentKeyIDWithKey(
                keystoreAccessForKeyStoreWithEncKey,
                documentKeyIDWithKey);
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
        DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest(factory);
        DocumentContent readDocumentContent = documentPersistenceServiceTest.testLoadDocument(documentGuardStuffForEncKey.documentGuardService,
                keystoreAccessForKeyStoreWithEncKey,
                documentLocation);
        Assert.assertEquals("Content of Document", readDocumentContent.toString(), documentContent.toString());
        LOGGER.debug("Document erfolgreich mit DocumentGuard für EncKey gelesen");

        return new FullStuff(keyStoreStuffForKeyStoreWithEncKey, documentGuardStuffForEncKey, null);

    }

}
