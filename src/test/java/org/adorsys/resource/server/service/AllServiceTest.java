package org.adorsys.resource.server.service;

import org.adorsys.encobject.utils.TestKeyUtils;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.ExtendedKeystorePersistence;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreID;
import org.adorsys.resource.server.persistence.basetypes.ReadKeyPassword;
import org.adorsys.resource.server.persistence.basetypes.ReadStorePassword;
import org.adorsys.resource.server.persistence.complextypes.DocumentKeyIDWithKey;
import org.adorsys.resource.server.persistence.complextypes.DocumentLocation;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreAccess;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreCreationConfig;
import org.adorsys.resource.server.utils.HexUtil;
import org.adorsys.resource.server.utils.ShowKeyStore;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by peter on 04.01.18.
 */
public class AllServiceTest {
    @BeforeClass
    public static void before() {
        TestKeyUtils.turnOffEncPolicy();
        KeyStoreServiceTest.beforeTest();
        DocumentGuardServiceTest.beforeClass();
        DocumentPersistenceServiceTest.beforeClass();
    }

    @AfterClass
    public static void after() {
        DocumentPersistenceServiceTest.afterClass();
        DocumentGuardServiceTest.afterClass();
        KeyStoreServiceTest.afterTest();

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

            System.out.println("DocumentKey is " + HexUtil.conventBytesToHexString(documentKeyIDWithKey.getDocumentKey().getSecretKey().getEncoded()));
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testCreateDocument() {
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
            new DocumentPersistenceServiceTest().testPersistDocument(
                    documentGuardStuff.documentGuardService,
                    documentKeyIDWithKey);
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testCreateAndLoadDocument() {
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
            DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest();
            DocumentPersistenceServiceTest.DocumentStuff documentStuff = new DocumentPersistenceServiceTest().testPersistDocument(
                    documentGuardStuff.documentGuardService,
                    documentKeyIDWithKey);
            documentPersistenceServiceTest.testLoadDocument(documentGuardStuff.documentGuardService,
                    keyStoreStuff.keyStoreAccess,
                    documentStuff.documentLocation);
            System.out.println("DocumentLocation     :" + documentStuff.documentLocation);
            System.out.println("DocumentKeyID        :" + documentKeyIDWithKey.getDocumentKeyID());
            System.out.println("KeyStoreLocation     :" + keyStoreStuff.keyStoreAccess.getKeyStoreLocation());
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testCreate_oneDocument_twoKeyStores_twoGuards_LoadDocument() {
        String container1 = "key-store-container-for-secretkey";
        String container2 = "key-store-container-for-enckey";
        try {
            DocumentKeyIDWithKey documentKeyIDWithKey;
            DocumentLocation documentLocation;
            {

                // Erzeugen eines ersten KeyStores nur mit SecretKey
                ExtendedKeystorePersistence keyStoreWithSecretKeyOnly = KeyStoreServiceTest.createKeyStorePersistenceForContainer(container1);
                KeyStoreServiceTest.KeyStoreStuff keyStoreStuffForKeyStoreWithSecretKey = new KeyStoreServiceTest().createKeyStore(keyStoreWithSecretKeyOnly,
                        container1,
                        new ReadStorePassword("a"),
                        new ReadKeyPassword("b"),
                        new KeyStoreID("first"),
                        new KeyStoreCreationConfig(0, 0, 1));
                System.out.println(ShowKeyStore.toString(keyStoreStuffForKeyStoreWithSecretKey.keyStore, keyStoreStuffForKeyStoreWithSecretKey.keyStoreAccess.getKeyStoreAuth().getReadKeyPassword()));
                System.out.println("Ersten KeyStore mit SecretKey erfolgreich angelegt");

                // Erzeugen des ersten DocumentGuards mit dem dem KeyStore, der den Secret Key enthält
                DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest();
                DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuffForSecretKey = documentGuardServiceTest.testCreateSymmetricDocumentGuard(
                        keyStoreStuffForKeyStoreWithSecretKey.keyStoreAccess,
                        keyStoreStuffForKeyStoreWithSecretKey.keystorePersistence);
                System.out.println("Ersten DocumentGuard mit secretKey verschlüsselt");

                // Erzeugen des Documents mit dem DocumentGuard, der mit dem secretKey verschlüsselt ist
                DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest();
                DocumentPersistenceServiceTest.DocumentStuff documentStuff = documentPersistenceServiceTest.testPersistDocument(
                        documentGuardStuffForSecretKey.documentGuardService,
                        documentGuardStuffForSecretKey.documentKeyIDWithKey);
                System.out.println("Document mit Schlüssel aus DocumentGuard verschlüsselt");

                // Laden des Documents mit dem KeyStore mit secretKey
                documentPersistenceServiceTest.testLoadDocument(documentGuardStuffForSecretKey.documentGuardService,
                        keyStoreStuffForKeyStoreWithSecretKey.keyStoreAccess,
                        documentStuff.documentLocation);
                System.out.println("Document mit DocumentGuard erfolgreich gelesen");

                // Etrahieren der DocumentID und der DocumentKeyID
                documentKeyIDWithKey = documentGuardStuffForSecretKey.documentKeyIDWithKey;
                documentLocation = documentStuff.documentLocation;
            }

            // Bis hier hin alles bekannt durch vorige Testfälle

            {
                // Anlegen des zweiten KeyStores. Nur mit einen EncKey
                ExtendedKeystorePersistence keyStoreWithEncKeyOnly = KeyStoreServiceTest.createKeyStorePersistenceForContainer(container2);
                KeyStoreServiceTest.KeyStoreStuff keyStoreStuffForKeyStoreWithEncKey = new KeyStoreServiceTest().createKeyStore(keyStoreWithEncKeyOnly,
                        container2,
                        new ReadStorePassword("c"),
                        new ReadKeyPassword("d"),
                        new KeyStoreID("second"),
                        new KeyStoreCreationConfig(1, 0, 0));
                System.out.println(ShowKeyStore.toString(keyStoreStuffForKeyStoreWithEncKey.keyStore, keyStoreStuffForKeyStoreWithEncKey.keyStoreAccess.getKeyStoreAuth().getReadKeyPassword()));
                System.out.println("Zweiten KeyStore mit EncKey erfolgreich angelegt");

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
                System.out.println("Zweiten DocumentGuard mit EncKey ohne Wissen über das Kennwort des Keys angelegt");

                // Jetzt Laden des Documents mit dem KeyStore, der mit dem EncKey verschlüsselt ist
                // Dazu muss der PrivateKey gelesen werden. Dazu muss das Kennwort zum Lesen der Keys wieder gesetzt sein
                keystoreAccessForKeyStoreWithEncKey.getKeyStoreAuth().setReadKeyPassword(readKeyPassword);

                // Load with asymmetric key
                DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest();
                documentPersistenceServiceTest.testLoadDocument(documentGuardStuffForEncKey.documentGuardService,
                        keystoreAccessForKeyStoreWithEncKey,
                        documentLocation);
                System.out.println("Document erfolgreich mit DocumentGuard für EncKey gelesen");

            }
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        } finally {
            KeyStoreServiceTest.removeContainer(container2);
            KeyStoreServiceTest.removeContainer(container1);
        }
    }
}
