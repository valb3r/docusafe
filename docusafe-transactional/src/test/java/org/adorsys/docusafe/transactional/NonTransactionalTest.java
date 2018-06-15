package org.adorsys.docusafe.transactional;

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
// @SuppressWarnings("Duplicates")
public class NonTransactionalTest extends TransactionFileStorageBaseTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(NonTransactionalTest.class);

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