package org.adorsys.resource.server.service;

import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.ExtendedKeystorePersistence;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreID;
import org.adorsys.resource.server.persistence.basetypes.ReadKeyPassword;
import org.adorsys.resource.server.persistence.basetypes.ReadStorePassword;
import org.adorsys.resource.server.persistence.complextypes.DocumentKeyIDWithKey;
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

    // @Test
    public void testCreateKeyStore() {
        try {
            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff = new KeyStoreServiceTest().createKeyStore();
            Assert.assertEquals("Number of Entries", 15, keyStoreStuff.keyStore.size());
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

    // @Test
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

    // @Test
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

    // @Test
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

    // @Test
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
        String container1 = "key-store-container-1";
        String container2 = "key-store-container-2";
        try {
            ExtendedKeystorePersistence keystorePersistence1 = KeyStoreServiceTest.createKeyStorePersistenceForContainer(container1);
            ExtendedKeystorePersistence keystorePersistence2 = KeyStoreServiceTest.createKeyStorePersistenceForContainer(container2);
            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff1 = new KeyStoreServiceTest().createKeyStore(keystorePersistence1,
                    container1,
                    new ReadStorePassword("a"),
                    new ReadKeyPassword("b"),
                    new KeyStoreID("first"),
                    new KeyStoreCreationConfig(0, 0, 1));
            System.out.println(ShowKeyStore.toString(keyStoreStuff1.keyStore, keyStoreStuff1.keyStoreAccess.getKeyStoreAuth().getReadKeyPassword()));

            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff2 = new KeyStoreServiceTest().createKeyStore(keystorePersistence2,
                    container2,
                    new ReadStorePassword("c"),
                    new ReadKeyPassword("d"),
                    new KeyStoreID("second"),
                    new KeyStoreCreationConfig(1, 0, 0));
            System.out.println(ShowKeyStore.toString(keyStoreStuff2.keyStore, keyStoreStuff2.keyStoreAccess.getKeyStoreAuth().getReadKeyPassword()));

            DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest();
            DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuff1 = documentGuardServiceTest.testCreateSymmetricDocumentGuard(
                    keyStoreStuff1.keyStoreAccess,
                    keyStoreStuff1.keystorePersistence);

            DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardStuff1.documentKeyIDWithKey;

            KeyStoreAccess keystore2Access = keyStoreStuff2.keyStoreAccess;
            keystore2Access.getKeyStoreAuth().deleteReadKeyPassword();
            DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuff2 = documentGuardServiceTest.testCreateAsymmetricDocumentGuardForDocumentKeyIDWithKey(
                    keystore2Access,
                    documentKeyIDWithKey,
                    keyStoreStuff2.keystorePersistence);

            DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest();
            DocumentPersistenceServiceTest.DocumentStuff documentStuff = documentPersistenceServiceTest.testPersistDocument(
                    documentGuardStuff1.documentGuardService,
                    documentGuardStuff1.documentKeyIDWithKey);

            // Load with symmetric key
            documentPersistenceServiceTest.testLoadDocument(documentGuardStuff1.documentGuardService,
                    keyStoreStuff1.keyStoreAccess,
                    documentStuff.documentLocation);


            // Load with asymmetric key
            documentPersistenceServiceTest.testLoadDocument(documentGuardStuff2.documentGuardService,
                    keyStoreStuff2.keyStoreAccess,
                    documentStuff.documentLocation);

            System.out.println("DocumentLocation     :" + documentStuff.documentLocation);
            System.out.println("DocumentKeyID        :" + documentGuardStuff1.documentKeyIDWithKey.getDocumentKeyID());
            System.out.println("KeyStoreLocation1     :" + keyStoreStuff1.keyStoreAccess.getKeyStoreLocation());
            System.out.println("KeyStoreLocation2     :" + keyStoreStuff2.keyStoreAccess.getKeyStoreLocation());

        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        } finally {
            KeyStoreServiceTest.removeContainer(container2);
            KeyStoreServiceTest.removeContainer(container1);
        }
    }
}
