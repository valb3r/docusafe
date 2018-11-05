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
    void grantAccessToNonTxFolder(UserIDAuth userIDAuth, UserID receiverUserID);

 *    void nonTxStoreDocument(UserIDAuth userIDAuth, DSDocument dsDocument);
 *   DSDocument nonTxReadDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);
 *   boolean nonTxDocumentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN);
    void nonTxDeleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);
 *   BucketContentFQN nonTxListDocuments(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag);

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
        BucketContentFQN bucketContentFQN = transactionalFileStorage.nonTxListDocuments(userIDAuth, documentFQN.getDocumentDirectory(), ListRecursiveFlag.TRUE);
        Assert.assertEquals(0, bucketContentFQN.getFiles().size());
        Assert.assertEquals(0, bucketContentFQN.getDirectories().size());
        Assert.assertFalse(transactionalFileStorage.nonTxDocumentExists(userIDAuth, documentFQN));

        {
            // Document speichern
            DocumentContent documentContent = new DocumentContent("content for in put box".getBytes());
            DSDocumentMetaInfo documentMetaInfo = new DSDocumentMetaInfo();
            storeDocument = new DSDocument(documentFQN, documentContent, documentMetaInfo);
            transactionalFileStorage.nonTxStoreDocument(userIDAuth, storeDocument);
            bucketContentFQN = transactionalFileStorage.nonTxListDocuments(userIDAuth, documentFQN.getDocumentDirectory(), ListRecursiveFlag.TRUE);
            Assert.assertEquals(1, bucketContentFQN.getFiles().size());
            Assert.assertEquals(0, bucketContentFQN.getDirectories().size());
            DSDocument readDocument = transactionalFileStorage.nonTxReadDocument(userIDAuth, documentFQN);
            Assert.assertArrayEquals(storeDocument.getDocumentContent().getValue(), readDocument.getDocumentContent().getValue());
            Assert.assertTrue(transactionalFileStorage.nonTxDocumentExists(userIDAuth, documentFQN));
        }

        {
            // Zugriff für andere nicht vorhanden
            transactionalFileStorage.createUser(systemUserIDAuth);
            CatchException.catchException(() -> transactionalFileStorage.nonTxReadDocument(systemUserIDAuth, userIDAuth.getUserID(), documentFQN));
            Assert.assertNotNull(CatchException.caughtException());
            CatchException.catchException(() -> transactionalFileStorage.nonTxDocumentExists(systemUserIDAuth, userIDAuth.getUserID(), documentFQN));
        }
        {
            // Zugriff gewähren
            transactionalFileStorage.grantAccessToNonTxFolder(userIDAuth, systemUserIDAuth.getUserID(), documentFQN.getDocumentDirectory());
            DSDocument readDocument = transactionalFileStorage.nonTxReadDocument(systemUserIDAuth, userIDAuth.getUserID(), documentFQN);
            Assert.assertArrayEquals(storeDocument.getDocumentContent().getValue(), readDocument.getDocumentContent().getValue());
            Assert.assertTrue(transactionalFileStorage.nonTxDocumentExists(systemUserIDAuth, userIDAuth.getUserID(), documentFQN));
        }
        {
            // Dokument Löschen
            transactionalFileStorage.nonTxDeleteDocument(userIDAuth, documentFQN);
            Assert.assertFalse(transactionalFileStorage.nonTxDocumentExists(userIDAuth, documentFQN));
        }

        transactionalFileStorage.destroyUser(userIDAuth);
        Assert.assertFalse(transactionalFileStorage.userExists(userIDAuth.getUserID()));
    }

    @Test
    public void testCreateUsersAndSendOneDocument() {
        

        transactionalFileStorage.createUser(userIDAuth);
        transactionalFileStorage.createUser(systemUserIDAuth);
        DocumentDirectoryFQN systemuserBaseDir = new DocumentDirectoryFQN("systemuser");
        transactionalFileStorage.grantAccessToNonTxFolder(userIDAuth, systemUserIDAuth.getUserID(), systemuserBaseDir);

        BucketContentFQN bucketContentFQN = transactionalFileStorage.nonTxListDocuments(userIDAuth, new DocumentDirectoryFQN("/"), ListRecursiveFlag.TRUE);
        bucketContentFQN.getDirectories().forEach(dir -> LOGGER.debug("dir: " + dir));
        bucketContentFQN.getFiles().forEach(dir -> LOGGER.debug("file: " + dir));
        Assert.assertEquals(0, bucketContentFQN.getFiles().size());
        Assert.assertEquals(0, bucketContentFQN.getDirectories().size());

        DocumentFQN documentFQN = systemuserBaseDir.addName("first.txt");
        DocumentContent documentContent = new DocumentContent("content for in put box".getBytes());
        DSDocumentMetaInfo documentMetaInfo = new DSDocumentMetaInfo();
        DSDocument document = new DSDocument(documentFQN, documentContent, documentMetaInfo);

        transactionalFileStorage.nonTxStoreDocument(systemUserIDAuth, userIDAuth.getUserID(), document);
        bucketContentFQN = transactionalFileStorage.nonTxListDocuments(userIDAuth, new DocumentDirectoryFQN("/"), ListRecursiveFlag.TRUE);
        bucketContentFQN.getDirectories().forEach(dir -> LOGGER.debug("dir: " + dir));
        bucketContentFQN.getFiles().forEach(dir -> LOGGER.debug("file: " + dir));
        Assert.assertEquals(1, bucketContentFQN.getFiles().size());
        Assert.assertEquals(1, bucketContentFQN.getDirectories().size());
    }
}
