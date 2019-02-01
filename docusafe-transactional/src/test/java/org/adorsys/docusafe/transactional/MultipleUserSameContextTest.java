package org.adorsys.docusafe.transactional;

import com.googlecode.catchexception.CatchException;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 01.02.19 10:59.
 */
public class MultipleUserSameContextTest extends TransactionFileStorageBaseTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(MultipleUserSameContextTest.class);

    // DOC-80
    @Test
    public void twoUsersCreateDocumenteInTheirOwnScopeButWithTheSameRequestContext() {
        {
            transactionalFileStorage.createUser(userIDAuth);
            transactionalFileStorage.createUser(systemUserIDAuth);

            DSDocument document1 = createDocument("file1");
            DSDocument document2 = createDocument("file2");

            LOGGER.debug("user1 starts TX");
            transactionalFileStorage.beginTransaction(userIDAuth);

            LOGGER.debug("user1 cant see the not yet created document " + document1.getDocumentFQN());
            Assert.assertFalse(transactionalFileStorage.txDocumentExists(userIDAuth, document1.getDocumentFQN()));

            LOGGER.debug("user1 creates " + document1.getDocumentFQN());
            transactionalFileStorage.txStoreDocument(userIDAuth, document1);

            LOGGER.debug("user1 can see his own documents " + document1.getDocumentFQN());
            Assert.assertTrue(transactionalFileStorage.txDocumentExists(userIDAuth, document1.getDocumentFQN()));

            LOGGER.debug("user2 starts TX");
            transactionalFileStorage.beginTransaction(systemUserIDAuth);

            LOGGER.debug("user2 cant see documents of user1");
            Assert.assertFalse(transactionalFileStorage.txDocumentExists(systemUserIDAuth, document1.getDocumentFQN()));

            LOGGER.debug("user1 ends TX");
            transactionalFileStorage.endTransaction(userIDAuth);

            LOGGER.debug("user2 still cant see documents of user1");
            Assert.assertFalse(transactionalFileStorage.txDocumentExists(systemUserIDAuth, document1.getDocumentFQN()));

            LOGGER.debug("user2 creates " + document2.getDocumentFQN());
            transactionalFileStorage.txStoreDocument(systemUserIDAuth, document2);

            LOGGER.debug("user2 cant see the new document");
            Assert.assertTrue(transactionalFileStorage.txDocumentExists(systemUserIDAuth, document2.getDocumentFQN()));

            LOGGER.debug("user1 cant do anything withoud opening another tx");
            CatchException.catchException(() -> transactionalFileStorage.txDocumentExists(userIDAuth, document1.getDocumentFQN()));
            Assert.assertNotNull(CatchException.caughtException());

            LOGGER.debug("user1 starts another TX");
            transactionalFileStorage.beginTransaction(userIDAuth);

            LOGGER.debug("user1 can see his own documents " + document1.getDocumentFQN());
            Assert.assertTrue(transactionalFileStorage.txDocumentExists(userIDAuth, document1.getDocumentFQN()));

            LOGGER.debug("user1 cant see documents of user2 " + document2.getDocumentFQN());
            Assert.assertFalse(transactionalFileStorage.txDocumentExists(userIDAuth, document2.getDocumentFQN()));

            LOGGER.debug("user2 ends TX");
            transactionalFileStorage.endTransaction(systemUserIDAuth);

            LOGGER.debug("user1 still cant see documents of user2 " + document2.getDocumentFQN());
            Assert.assertFalse(transactionalFileStorage.txDocumentExists(userIDAuth, document2.getDocumentFQN()));

            LOGGER.debug("user1 ends TX");
            transactionalFileStorage.endTransaction(userIDAuth);

            LOGGER.debug("user1 starts another TX");
            transactionalFileStorage.beginTransaction(userIDAuth);

            LOGGER.debug("user2 starts another TX");
            transactionalFileStorage.beginTransaction(systemUserIDAuth);

            LOGGER.debug("user1 will never see documents of user2 " + document2.getDocumentFQN());
            Assert.assertFalse(transactionalFileStorage.txDocumentExists(userIDAuth, document2.getDocumentFQN()));

            LOGGER.debug("user2 will never see documents of user1 " + document1.getDocumentFQN());
            Assert.assertFalse(transactionalFileStorage.txDocumentExists(systemUserIDAuth, document1.getDocumentFQN()));
        }
    }

    private DSDocument createDocument(String name) {
        DocumentFQN documentFQN = new DocumentFQN(name);
        DocumentContent documentContent = new DocumentContent(("CONTENT OF FILE " + name).getBytes());
        DSDocumentMetaInfo documentMetaInfo = new DSDocumentMetaInfo();
        return new DSDocument(documentFQN, documentContent, documentMetaInfo);
    }
}
