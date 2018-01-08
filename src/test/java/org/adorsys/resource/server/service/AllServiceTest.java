package org.adorsys.resource.server.service;

import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.ExtendedKeystorePersistence;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreID;
import org.adorsys.resource.server.persistence.complextypes.DocumentKeyIDWithKey;
import org.adorsys.resource.server.utils.HexUtil;
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
            new DocumentGuardServiceTest().testCreateDocumentGuard(
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
            DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuff = documentGuardServiceTest.testCreateDocumentGuard(
                    keyStoreStuff.keyStoreAccess,
                    keyStoreStuff.keystorePersistence);
            DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardServiceTest.testLoadDocumentGuard(
                    keyStoreStuff.keyStoreAccess,
                    keyStoreStuff.keystorePersistence,
                    documentGuardStuff.documentKeyID);

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
            DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuff = documentGuardServiceTest.testCreateDocumentGuard(
                    keyStoreStuff.keyStoreAccess,
                    keyStoreStuff.keystorePersistence);
            DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardServiceTest.testLoadDocumentGuard(
                    keyStoreStuff.keyStoreAccess,
                    keyStoreStuff.keystorePersistence,
                    documentGuardStuff.documentKeyID);
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
            DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuff = documentGuardServiceTest.testCreateDocumentGuard(
                    keyStoreStuff.keyStoreAccess,
                    keyStoreStuff.keystorePersistence);
            DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardServiceTest.testLoadDocumentGuard(
                    keyStoreStuff.keyStoreAccess,
                    keyStoreStuff.keystorePersistence,
                    documentGuardStuff.documentKeyID);
            DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest();
            DocumentPersistenceServiceTest.DocumentStuff documentStuff = new DocumentPersistenceServiceTest().testPersistDocument(
                    documentGuardStuff.documentGuardService,
                    documentKeyIDWithKey);
            documentPersistenceServiceTest.testLoadDocument(documentGuardStuff.documentGuardService,
                    keyStoreStuff.keyStoreAccess,
                    documentStuff.documentLocation);
            System.out.println("DocumentLocation     :" + documentStuff.documentLocation);
            System.out.println("DocumentKeyID        :" + documentGuardStuff.documentKeyID);
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
            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff1 = new KeyStoreServiceTest().createKeyStore(keystorePersistence1, container1, "a", "b", new KeyStoreID("first"));
            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff2 = new KeyStoreServiceTest().createKeyStore(keystorePersistence2, container2, "c", "d", new KeyStoreID("second"));

            DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest();
            DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuff1 = documentGuardServiceTest.testCreateDocumentGuard(
                    keyStoreStuff1.keyStoreAccess,
                    keyStoreStuff1.keystorePersistence);
            DocumentKeyIDWithKey documentKeyIDWithKey1 = documentGuardServiceTest.testLoadDocumentGuard(
                    keyStoreStuff1.keyStoreAccess,
                    keyStoreStuff1.keystorePersistence,
                    documentGuardStuff1.documentKeyID);

            DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuff2 = documentGuardServiceTest.testCreateDocumentGuardForDocumentKeyIDWithKey(
                    keyStoreStuff2.keyStoreAccess,
                    documentKeyIDWithKey1,
                    keyStoreStuff2.keystorePersistence);
            DocumentKeyIDWithKey documentKeyIDWithKey2 = documentGuardServiceTest.testLoadDocumentGuard(
                    keyStoreStuff2.keyStoreAccess,
                    keyStoreStuff2.keystorePersistence,
                    documentGuardStuff2.documentKeyID);

            Assert.assertEquals("keys of different guards", documentKeyIDWithKey1.getDocumentKey(), documentKeyIDWithKey2.getDocumentKey());
            Assert.assertEquals("key ids of different guards", documentKeyIDWithKey1.getDocumentKeyID(), documentKeyIDWithKey2.getDocumentKeyID());

            DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest();
            DocumentPersistenceServiceTest.DocumentStuff documentStuff = documentPersistenceServiceTest.testPersistDocument(
                    documentGuardStuff1.documentGuardService,
                    documentKeyIDWithKey1);
            documentPersistenceServiceTest.testLoadDocument(documentGuardStuff1.documentGuardService,
                    keyStoreStuff1.keyStoreAccess,
                    documentStuff.documentLocation);
            System.out.println("DocumentLocation     :" + documentStuff.documentLocation);
            System.out.println("DocumentKeyID        :" + documentGuardStuff1.documentKeyID);
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
