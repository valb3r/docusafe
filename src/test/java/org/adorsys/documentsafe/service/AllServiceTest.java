package org.adorsys.documentsafe.service;

import org.adorsys.documentsafe.layer00common.exceptions.BaseException;
import org.adorsys.documentsafe.layer00common.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer00common.utils.HexUtil;
import org.adorsys.documentsafe.layer01persistence.ExtendedBlobStoreConnection;
import org.adorsys.documentsafe.layer01persistence.ExtendedKeystorePersistence;
import org.adorsys.documentsafe.layer01persistence.exceptions.FileExistsException;
import org.adorsys.documentsafe.layer01persistence.types.BucketName;
import org.adorsys.documentsafe.layer01persistence.types.KeyStoreID;
import org.adorsys.documentsafe.layer01persistence.types.OverwriteFlag;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.adorsys.documentsafe.layer02service.generators.KeyStoreCreationConfig;
import org.adorsys.documentsafe.layer02service.types.DocumentContent;
import org.adorsys.documentsafe.layer02service.types.DocumentID;
import org.adorsys.documentsafe.layer02service.types.ReadKeyPassword;
import org.adorsys.documentsafe.layer02service.types.ReadStorePassword;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentBucketPath;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentLocation;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.documentsafe.layer02service.utils.ShowKeyStore;
import org.adorsys.encobject.service.BlobStoreConnection;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.utils.TestFsBlobStoreFactory;
import org.adorsys.encobject.utils.TestKeyUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 04.01.18.
 */
public class AllServiceTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(AllServiceTest.class);

    @Before
    public void before() {

        TestKeyUtils.turnOffEncPolicy();
        KeyStoreServiceTest.beforeTest();
        DocumentGuardServiceTest.beforeClass();
        DocumentPersistenceServiceTest.beforeClass();
    }

    @After
    public void after() {
        DocumentPersistenceServiceTest.afterClass();
        DocumentGuardServiceTest.afterClass();
        KeyStoreServiceTest.afterTest();

    }

    @Test
    public void testCreateBucketPath() {
        try {
            BucketPath bp = new BucketPath().set(new BucketName("1")).sub(new BucketName("2")).sub(new BucketName("3"));
            TestFsBlobStoreFactory storeContextFactory = new TestFsBlobStoreFactory();
            ExtendedKeystorePersistence keystorePersistence = new ExtendedKeystorePersistence(storeContextFactory);
            ContainerPersistence containerPersistence = new ContainerPersistence(new ExtendedBlobStoreConnection(storeContextFactory));
            containerPersistence.creteContainer(bp.getObjectHandlePath());
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        } finally {
            KeyStoreServiceTest.removeContainer("1");
        }
    }

    @Test
    public void testCreateKeyStore() {
        try {
            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff = new KeyStoreServiceTest().createKeyStore();
            Assert.assertEquals("Number of Entries", 15, keyStoreStuff.keyStore.size());
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testCreateKeyStoreAndDocumentGuard() {
        try {
            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff = new KeyStoreServiceTest().createKeyStore();
            new DocumentGuardServiceTest().testCreateSymmetricDocumentGuard(
                    keyStoreStuff.keyStoreAccess,
                    keyStoreStuff.keystorePersistence);
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testCreateKeyStoreAndDocumentGuardAndLoadDocumentGuard() {
        try {
            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff = new KeyStoreServiceTest().createKeyStore();
            DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest();
            DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuff = documentGuardServiceTest.testCreateSymmetricDocumentGuard(
                    keyStoreStuff.keyStoreAccess,
                    keyStoreStuff.keystorePersistence);
            DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardServiceTest.testLoadDocumentGuard(
                    keyStoreStuff.keyStoreAccess,
                    keyStoreStuff.keystorePersistence,
                    documentGuardStuff.documentKeyIDWithKey.getDocumentKeyID());

            LOGGER.info("DocumentKey is " + HexUtil.conventBytesToHexString(documentKeyIDWithKey.getDocumentKey().getSecretKey().getEncoded()));
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testCreateDocument() {
        try {
            DocumentContent documentContent = new DocumentContent("Ein Affe im Zoo ist nie allein".getBytes());

            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff = new KeyStoreServiceTest().createKeyStore();
            DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest();
            DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuff = documentGuardServiceTest.testCreateSymmetricDocumentGuard(
                    keyStoreStuff.keyStoreAccess,
                    keyStoreStuff.keystorePersistence);
            DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardServiceTest.testLoadDocumentGuard(
                    keyStoreStuff.keyStoreAccess,
                    keyStoreStuff.keystorePersistence,
                    documentGuardStuff.documentKeyIDWithKey.getDocumentKeyID());
            new DocumentPersistenceServiceTest().testPersistDocument(
                    documentGuardStuff.documentGuardService,
                    new DocumentBucketPath("documentBucketPath1"),
                    documentKeyIDWithKey, documentContent);
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testCreateAndLoadDocument() {
        try {
            DocumentContent documentContent = new DocumentContent("Ein Affe im Zoo ist nie allein".getBytes());

            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff = new KeyStoreServiceTest().createKeyStore();
            DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest();
            DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuff = documentGuardServiceTest.testCreateSymmetricDocumentGuard(
                    keyStoreStuff.keyStoreAccess,
                    keyStoreStuff.keystorePersistence);
            DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardServiceTest.testLoadDocumentGuard(
                    keyStoreStuff.keyStoreAccess,
                    keyStoreStuff.keystorePersistence,
                    documentGuardStuff.documentKeyIDWithKey.getDocumentKeyID());
            DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest();
            DocumentPersistenceServiceTest.DocumentStuff documentStuff = new DocumentPersistenceServiceTest().testPersistDocument(
                    documentGuardStuff.documentGuardService,
                    new DocumentBucketPath("documentBucketPath2"),
                    documentKeyIDWithKey,
                    documentContent);
            DocumentContent readContent = documentPersistenceServiceTest.testLoadDocument(documentGuardStuff.documentGuardService,
                    keyStoreStuff.keyStoreAccess,
                    documentStuff.documentLocation);

            Assert.assertEquals("Content of Document", readContent.toString(), documentContent.toString());

            LOGGER.info("DocumentLocation     :" + documentStuff.documentLocation);
            LOGGER.info("DocumentKeyID        :" + documentKeyIDWithKey.getDocumentKeyID());
            LOGGER.info("KeyStoreLocation     :" + keyStoreStuff.keyStoreAccess.getKeyStoreLocation());
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
        String container1 = "user1/key-store-container-for-secretkey";
        String container2 = "user2/key-store-container-for-enckey";
        DocumentBucketPath documentBucketPath = new DocumentBucketPath("documentBucketPath3/subfolder/1/2/3");
        DocumentContent documentContent = new DocumentContent("Ein Affe im Zoo ist nie allein".getBytes());

        try {
            FullStuff symmetricStuff = createKeyStoreAndDocument(container1, documentBucketPath, documentContent);
            createPublicKeyStoreForKnownDocument(container2, documentContent, symmetricStuff.documentStuff.documentLocation, symmetricStuff.documentGuardStuff.documentKeyIDWithKey, true);
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        } finally {
            KeyStoreServiceTest.removeContainer(container2);
            KeyStoreServiceTest.removeContainer(container1);
        }
    }

    /**
     * wie der vorige Test, nur dass der SecretKey nicht gelesen werden können soll,
     * weil er nicht bekannt ist. Daher expected Excption
     */
    @Test(expected = BaseException.class)
    public void testCreate_oneDocument_twoKeyStores_twoGuards_LoadDocument_with_expected_failure() {
        String container1 = "user/8/key-store-container-for-secretkey";
        String container2 = "user/9/key-store-container-for-enckey";
        DocumentBucketPath documentBucketPath = new DocumentBucketPath("documentBucketPath/4");

        DocumentContent documentContent = new DocumentContent("Ein Affe im Zoo ist nie allein".getBytes());

        try {
            FullStuff symmetricStuff = createKeyStoreAndDocument(container1, documentBucketPath, documentContent);
            createPublicKeyStoreForKnownDocument(container2, documentContent, symmetricStuff.documentStuff.documentLocation, symmetricStuff.documentGuardStuff.documentKeyIDWithKey, false);
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        } finally {
            KeyStoreServiceTest.removeContainer(container2);
            KeyStoreServiceTest.removeContainer(container1);
        }
    }

    @Test
    public void testCreate_oneDocument_twoKeyStores_twoGuards_ChangeDocument() {
        String container1 = "user1/key-store-container-for-secretkey";
        String container2 = "user2/key-store-container-for-enckey";
        DocumentBucketPath documentBucketPath = new DocumentBucketPath("documentBucketPath5/1/2/3");

        DocumentContent documentContent = new DocumentContent("Ein Affe im Zoo ist nie allein".getBytes());
        try {
            FullStuff symmetricStuff = createKeyStoreAndDocument(container1, documentBucketPath, documentContent);
            FullStuff asymmetricStuff = createPublicKeyStoreForKnownDocument(container2, documentContent, symmetricStuff.documentStuff.documentLocation, symmetricStuff.documentGuardStuff.documentKeyIDWithKey, true);

            // Neuer Inhalt für das Document, für das es bereits zwei Guards gibt
            DocumentContent newDocumentContent = new DocumentContent("ein anderer affe im zoo".getBytes());
            DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest();
            DocumentPersistenceServiceTest.DocumentStuff documentStuff = documentPersistenceServiceTest.testPersistDocument(
                    asymmetricStuff.documentGuardStuff.documentGuardService,
                    documentBucketPath,
                    asymmetricStuff.documentGuardStuff.documentKeyIDWithKey,
                    symmetricStuff.documentStuff.documentID,
                    newDocumentContent,
                    OverwriteFlag.TRUE
            );
            LOGGER.info("Document erfolgreich ERNEUT geschrieben");

            // Load with asymmetric key
            DocumentContent newReadDocumentContent = documentPersistenceServiceTest.testLoadDocument(
                    asymmetricStuff.documentGuardStuff.documentGuardService,
                    asymmetricStuff.keyStoreStuff.keyStoreAccess,
                    documentStuff.documentLocation);
            Assert.assertEquals("Content of Document", newDocumentContent.toString(), newReadDocumentContent.toString());
            LOGGER.info("Document erfolgreich mit DocumentGuard für EncKey gelesen");

        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        } finally {
            KeyStoreServiceTest.removeContainer(container2);
            KeyStoreServiceTest.removeContainer(container1);
        }
    }

    @Test(expected = FileExistsException.class)
    public void testCreateDocumentTwice() {
        DocumentBucketPath documentBucketPath = new DocumentBucketPath("user1/bucket/folder1");
        DocumentContent documentContent = new DocumentContent("Affe".getBytes());

        DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest();
        DocumentKeyIDWithKey keyIDWithKey = documentGuardServiceTest.createKeyIDWithKey();

        DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest();
        DocumentID documentID = new DocumentID("AffenDocument-1");
        documentPersistenceServiceTest.testPersistDocument(null, documentBucketPath, keyIDWithKey, documentID, documentContent, OverwriteFlag.FALSE);
        documentPersistenceServiceTest.testPersistDocument(null, documentBucketPath, keyIDWithKey, documentID, documentContent, OverwriteFlag.FALSE);
    }

    @Test
    public void testCreateDocumentTwiceButOverwrite() {
        DocumentBucketPath documentBucketPath = new DocumentBucketPath("user1/bucket/folder1");
        DocumentContent documentContent = new DocumentContent("Affe".getBytes());

        DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest();
        DocumentKeyIDWithKey keyIDWithKey = documentGuardServiceTest.createKeyIDWithKey();

        DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest();
        DocumentID documentID = new DocumentID("AffenDocument-1");
        documentPersistenceServiceTest.testPersistDocument(null, documentBucketPath, keyIDWithKey, documentID, documentContent, OverwriteFlag.FALSE);
        documentPersistenceServiceTest.testPersistDocument(null, documentBucketPath, keyIDWithKey, documentID, documentContent, OverwriteFlag.TRUE);

    }

    @Test
    public void testBucketService1() {
        BucketServiceTest.beforeClass();

        List<DocumentBucketPath> documentBucketPathList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            documentBucketPathList.add(new DocumentBucketPath("user1/bucket/folder1/" + i));
        }
        DocumentContent documentContent = new DocumentContent("Affe".getBytes());

        BucketServiceTest bucketServiceTest = new BucketServiceTest();
        bucketServiceTest.createBucket(documentBucketPathList.get(0));

        BlobStoreConnection blobStoreConnection = new ExtendedBlobStoreConnection(new TestFsBlobStoreFactory());

        DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest();
        DocumentKeyIDWithKey keyIDWithKey = documentGuardServiceTest.createKeyIDWithKey();

        DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest();
        for (int i = 0; i < documentBucketPathList.size(); i++) {
            for (int j = 0; j < 5; j++) {
                DocumentID documentID = new DocumentID("AffenDocument" + j);
                documentPersistenceServiceTest.testPersistDocument(null, documentBucketPathList.get(i), keyIDWithKey, documentID, documentContent, OverwriteFlag.FALSE);
            }
        }

        BucketServiceTest.afterClass();
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
        ExtendedKeystorePersistence keyStoreWithSecretKeyOnly = KeyStoreServiceTest.createKeyStorePersistenceForContainer(container1);
        KeyStoreServiceTest.KeyStoreStuff keyStoreStuffForKeyStoreWithSecretKey = new KeyStoreServiceTest().createKeyStore(keyStoreWithSecretKeyOnly,
                container1,
                new ReadStorePassword("a"),
                new ReadKeyPassword("b"),
                new KeyStoreID("first"),
                new KeyStoreCreationConfig(0, 0, 1));
        LOGGER.info(ShowKeyStore.toString(keyStoreStuffForKeyStoreWithSecretKey.keyStore, keyStoreStuffForKeyStoreWithSecretKey.keyStoreAccess.getKeyStoreAuth().getReadKeyPassword()));
        LOGGER.info("Ersten KeyStore mit SecretKey erfolgreich angelegt");

        // Erzeugen des ersten DocumentGuards mit dem dem KeyStore, der den Secret Key enthält
        DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest();
        DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuffForSecretKey = documentGuardServiceTest.testCreateSymmetricDocumentGuard(
                keyStoreStuffForKeyStoreWithSecretKey.keyStoreAccess,
                keyStoreStuffForKeyStoreWithSecretKey.keystorePersistence);
        LOGGER.info("Ersten DocumentGuard mit secretKey verschlüsselt");

        // Erzeugen des Documents mit dem DocumentGuard, der mit dem secretKey verschlüsselt ist
        DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest();
        DocumentPersistenceServiceTest.DocumentStuff documentStuff = documentPersistenceServiceTest.testPersistDocument(
                documentGuardStuffForSecretKey.documentGuardService,
                documentBucketPath,
                documentGuardStuffForSecretKey.documentKeyIDWithKey,
                documentContent);
        LOGGER.info("Document mit Schlüssel aus DocumentGuard verschlüsselt");

        // Laden des Documents mit dem KeyStore mit secretKey
        DocumentContent readDocumentContent = documentPersistenceServiceTest.testLoadDocument(documentGuardStuffForSecretKey.documentGuardService,
                keyStoreStuffForKeyStoreWithSecretKey.keyStoreAccess,
                documentStuff.documentLocation);
        Assert.assertEquals("Content of Document", readDocumentContent.toString(), documentContent.toString());

        LOGGER.info("Document mit DocumentGuard erfolgreich gelesen");

        return new FullStuff(keyStoreStuffForKeyStoreWithSecretKey, documentGuardStuffForSecretKey, documentStuff);
    }

    private FullStuff createPublicKeyStoreForKnownDocument(String container2, DocumentContent documentContent, DocumentLocation documentLocation, DocumentKeyIDWithKey documentKeyIDWithKey, boolean setReadKeyPassword) {
        // Anlegen des zweiten KeyStores. Nur mit einen EncKey
        ExtendedKeystorePersistence keyStoreWithEncKeyOnly = KeyStoreServiceTest.createKeyStorePersistenceForContainer(container2);
        KeyStoreServiceTest.KeyStoreStuff keyStoreStuffForKeyStoreWithEncKey = new KeyStoreServiceTest().createKeyStore(keyStoreWithEncKeyOnly,
                container2,
                new ReadStorePassword("c"),
                new ReadKeyPassword("d"),
                new KeyStoreID("second"),
                new KeyStoreCreationConfig(1, 0, 0));
        LOGGER.info(ShowKeyStore.toString(keyStoreStuffForKeyStoreWithEncKey.keyStore, keyStoreStuffForKeyStoreWithEncKey.keyStoreAccess.getKeyStoreAuth().getReadKeyPassword()));
        LOGGER.info("Zweiten KeyStore mit EncKey erfolgreich angelegt");

        // vorher sicherheitshalber Löschen des Kennworts zum Lesen der Keys
        KeyStoreAccess keystoreAccessForKeyStoreWithEncKey = keyStoreStuffForKeyStoreWithEncKey.keyStoreAccess;
        ReadKeyPassword readKeyPassword = keystoreAccessForKeyStoreWithEncKey.getKeyStoreAuth().getReadKeyPassword();
        keystoreAccessForKeyStoreWithEncKey.getKeyStoreAuth().setReadKeyPassword(null);

        // Jetzt Erzeugen des zweiten DocumentGuards mit dem KeyStore, der den EncKey enthält
        DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest();
        DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuffForEncKey = documentGuardServiceTest.testCreateAsymmetricDocumentGuardForDocumentKeyIDWithKey(
                keystoreAccessForKeyStoreWithEncKey,
                documentKeyIDWithKey,
                keyStoreStuffForKeyStoreWithEncKey.keystorePersistence);
        LOGGER.info("Zweiten DocumentGuard mit EncKey ohne Wissen über das Kennwort des Keys angelegt");

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
        DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest();
        DocumentContent readDocumentContent = documentPersistenceServiceTest.testLoadDocument(documentGuardStuffForEncKey.documentGuardService,
                keystoreAccessForKeyStoreWithEncKey,
                documentLocation);
        Assert.assertEquals("Content of Document", readDocumentContent.toString(), documentContent.toString());
        LOGGER.info("Document erfolgreich mit DocumentGuard für EncKey gelesen");

        return new FullStuff(keyStoreStuffForKeyStoreWithEncKey, documentGuardStuffForEncKey, null);

    }

}
