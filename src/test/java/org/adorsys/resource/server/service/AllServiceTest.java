package org.adorsys.resource.server.service;

import org.adorsys.resource.server.basetypes.DocumentContent;
import org.adorsys.resource.server.basetypes.DocumentID;
import org.adorsys.resource.server.complextypes.DocumentGuard;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.basetypes.BucketName;
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
                    keyStoreStuff.userKeyStoreHandler,
                    keyStoreStuff.keyPassHandler,
                    keyStoreStuff.keystorePersistence,
                    keyStoreStuff.keyStoreBucketName,
                    keyStoreStuff.keyStoreID);
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
                    keyStoreStuff.userKeyStoreHandler,
                    keyStoreStuff.keyPassHandler,
                    keyStoreStuff.keystorePersistence,
                    keyStoreStuff.keyStoreBucketName,
                    keyStoreStuff.keyStoreID);
            DocumentGuard documentGuard = documentGuardServiceTest.testLoadDocumentGuard(
                    keyStoreStuff.userKeyStoreHandler,
                    keyStoreStuff.keyPassHandler,
                    keyStoreStuff.keystorePersistence,
                    documentGuardStuff.documentGuardName);

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
                    keyStoreStuff.userKeyStoreHandler,
                    keyStoreStuff.keyPassHandler,
                    keyStoreStuff.keystorePersistence,
                    keyStoreStuff.keyStoreBucketName,
                    keyStoreStuff.keyStoreID);
            DocumentGuard documentGuard = documentGuardServiceTest.testLoadDocumentGuard(
                    keyStoreStuff.userKeyStoreHandler,
                    keyStoreStuff.keyPassHandler,
                    keyStoreStuff.keystorePersistence,
                    documentGuardStuff.documentGuardName);
            new DocumentPersistenceServiceTest().testPersistDocument(
                    documentGuardStuff.documentGuardService,
                    keyStoreStuff.userKeyStoreHandler,
                    keyStoreStuff.keyPassHandler,
                    documentGuardStuff.documentGuardName);
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }


    @Test
    public void testPersistAndLoadDocument() {
        try {
            BucketName documentBucketName = new BucketName("document-bucket");
            DocumentID documentID = new DocumentID("document-id-123");
            DocumentContent documentContent = new DocumentContent("Der Inhalt ist ein Affe".getBytes());
            KeyStoreServiceTest.KeyStoreStuff keyStoreStuff = new KeyStoreServiceTest().createKeyStore();
            DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest();
            DocumentGuardServiceTest.DocumentGuardStuff documentGuardStuff = documentGuardServiceTest.testCreateDocumentGuard(
                    keyStoreStuff.userKeyStoreHandler,
                    keyStoreStuff.keyPassHandler,
                    keyStoreStuff.keystorePersistence,
                    keyStoreStuff.keyStoreBucketName,
                    keyStoreStuff.keyStoreID);
            DocumentGuard documentGuard = documentGuardServiceTest.testLoadDocumentGuard(
                    keyStoreStuff.userKeyStoreHandler,
                    keyStoreStuff.keyPassHandler,
                    keyStoreStuff.keystorePersistence,
                    documentGuardStuff.documentGuardName);
            DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest();
            DocumentPersistenceServiceTest.DocumentStuff documentStuff = documentPersistenceServiceTest.testPersistDocument(
                    documentGuardStuff.documentGuardService,
                    keyStoreStuff.userKeyStoreHandler,
                    keyStoreStuff.keyPassHandler,
                    documentGuardStuff.documentGuardName);
            documentPersistenceServiceTest.testLoadDocument(documentGuardStuff.documentGuardService,
                    keyStoreStuff.userKeyStoreHandler,
                    keyStoreStuff.keyPassHandler,
                    keyStoreStuff.keyStoreName,
                    documentStuff.documentBucketName,
                    documentStuff.documentID);
        } catch (Exception e) {
            BaseExceptionHandler.handle(e);
        }
    }

}
