package org.adorsys.resource.server.service;

import org.adorsys.resource.server.persistence.complextypes.DocumentGuard;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
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
            DocumentGuard documentGuard = documentGuardServiceTest.testLoadDocumentGuard(
                    keyStoreStuff.keyStoreAccess,
                    keyStoreStuff.keystorePersistence,
                    documentGuardStuff.documentKeyID);

            System.out.println("DocumentKey is " + HexUtil.conventBytesToHexString(documentGuard.getDocumentKey().getSecretKey().getEncoded()));
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
            new DocumentPersistenceServiceTest().testPersistDocument(
                    documentGuardStuff.documentGuardService,
                    keyStoreStuff.keyStoreAccess,
                    documentGuardStuff.documentKeyID);
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
            DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest();
            DocumentPersistenceServiceTest.DocumentStuff documentStuff = documentPersistenceServiceTest.testPersistDocument(
                    documentGuardStuff.documentGuardService,
                    keyStoreStuff.keyStoreAccess,
                    documentGuardStuff.documentKeyID);
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

}
