package org.adorsys.docusafe.transactional;

import com.googlecode.catchexception.CatchException;
import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 13.06.18 at 19:42.
 */
/*
    // NON-TRANSACTIONAL FOR OWNER
 *   void createUser(UserIDAuth userIDAuth);
 *   void destroyUser(UserIDAuth userIDAuth);
 *   boolean userExists(UserID userID);
    void grantAccess(UserIDAuth userIDAuth, UserID receiverUserID);

 *    void storeDocument(UserIDAuth userIDAuth, DSDocument dsDocument);
 *   DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);
 *   boolean documentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN);
    void deleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);
 *   BucketContentFQN listDocuments(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag);

 */
// @SuppressWarnings("Duplicates")
public class NonTransactionalTest extends TransactionFileStorageBaseTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(NonTransactionalTest.class);

    @Test
    public void testAllNonTransactional() {
        transactionalFileStorage.createUser(userIDAuth);
        Assert.assertTrue(transactionalFileStorage.userExists(userIDAuth.getUserID()));

        DocumentFQN documentFQN = new DocumentFQN("file1.txt");
        DSDocument storeDocument = null;
        BucketContentFQN bucketContentFQN = transactionalFileStorage.listDocuments(userIDAuth, documentFQN.getDocumentDirectory(), ListRecursiveFlag.TRUE);
        Assert.assertEquals(0, bucketContentFQN.getFiles().size());
        Assert.assertEquals(0, bucketContentFQN.getDirectories().size());
        Assert.assertFalse(transactionalFileStorage.documentExists(userIDAuth, documentFQN));

        {
            // Document speichern
            DocumentContent documentContent = new DocumentContent("content for in put box".getBytes());
            DSDocumentMetaInfo documentMetaInfo = new DSDocumentMetaInfo();
            storeDocument = new DSDocument(documentFQN, documentContent, documentMetaInfo);
            transactionalFileStorage.storeDocument(userIDAuth, storeDocument);
            bucketContentFQN = transactionalFileStorage.listDocuments(userIDAuth, documentFQN.getDocumentDirectory(), ListRecursiveFlag.TRUE);
            Assert.assertEquals(1, bucketContentFQN.getFiles().size());
            Assert.assertEquals(0, bucketContentFQN.getDirectories().size());
            DSDocument readDocument = transactionalFileStorage.readDocument(userIDAuth, documentFQN);
            Assert.assertArrayEquals(storeDocument.getDocumentContent().getValue(), readDocument.getDocumentContent().getValue());
            Assert.assertTrue(transactionalFileStorage.documentExists(userIDAuth, documentFQN));
        }

        {
            // Zugriff für andere nicht vorhanden
            transactionalFileStorage.createUser(systemUserIDAuth);
            CatchException.catchException(() -> transactionalFileStorage.readDocument(systemUserIDAuth, userIDAuth.getUserID(), documentFQN));
            Assert.assertNotNull(CatchException.caughtException());
            CatchException.catchException(() -> transactionalFileStorage.documentExists(systemUserIDAuth, userIDAuth.getUserID(), documentFQN));
        }
        {
            // Zugriff gewähren
            transactionalFileStorage.grantAccess(userIDAuth, systemUserIDAuth.getUserID());
            DSDocument readDocument = transactionalFileStorage.readDocument(systemUserIDAuth, userIDAuth.getUserID(), documentFQN);
            Assert.assertArrayEquals(storeDocument.getDocumentContent().getValue(), readDocument.getDocumentContent().getValue());
            Assert.assertTrue(transactionalFileStorage.documentExists(systemUserIDAuth, userIDAuth.getUserID(), documentFQN));
        }
        {
            // Dokument Löschen
            transactionalFileStorage.deleteDocument(userIDAuth, documentFQN);
            Assert.assertFalse(transactionalFileStorage.documentExists(userIDAuth, documentFQN));
        }

        transactionalFileStorage.destroyUser(userIDAuth);
        Assert.assertFalse(transactionalFileStorage.userExists(userIDAuth.getUserID()));
    }

    @Test
    public void testCreateUsersAndSendOneDocument() {
        transactionalFileStorage.createUser(userIDAuth);
        transactionalFileStorage.createUser(systemUserIDAuth);
        transactionalFileStorage.grantAccess(userIDAuth, systemUserIDAuth.getUserID());

        BucketContentFQN bucketContentFQN = transactionalFileStorage.listDocuments(userIDAuth, new DocumentDirectoryFQN("/"), ListRecursiveFlag.TRUE);
        bucketContentFQN.getDirectories().forEach(dir -> LOGGER.debug("dir: " + dir));
        bucketContentFQN.getFiles().forEach(dir -> LOGGER.debug("file: " + dir));
        Assert.assertEquals(0, bucketContentFQN.getFiles().size());
        Assert.assertEquals(0, bucketContentFQN.getDirectories().size());

        DocumentFQN documentFQN = new DocumentFQN("first.txt");
        DocumentContent documentContent = new DocumentContent("content for in put box".getBytes());
        DSDocumentMetaInfo documentMetaInfo = new DSDocumentMetaInfo();
        DSDocument document = new DSDocument(documentFQN, documentContent, documentMetaInfo);

        transactionalFileStorage.storeDocument(systemUserIDAuth, userIDAuth.getUserID(), document);
        bucketContentFQN = transactionalFileStorage.listDocuments(userIDAuth, new DocumentDirectoryFQN("/"), ListRecursiveFlag.TRUE);
        bucketContentFQN.getDirectories().forEach(dir -> LOGGER.debug("dir: " + dir));
        bucketContentFQN.getFiles().forEach(dir -> LOGGER.debug("file: " + dir));
        Assert.assertEquals(1, bucketContentFQN.getFiles().size());
        Assert.assertEquals(0, bucketContentFQN.getDirectories().size());
    }
}