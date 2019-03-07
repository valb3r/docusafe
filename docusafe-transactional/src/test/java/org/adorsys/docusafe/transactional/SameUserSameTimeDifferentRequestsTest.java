package org.adorsys.docusafe.transactional;

import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 07.03.19 11:22.
 */
public class SameUserSameTimeDifferentRequestsTest extends TransactionalDocumentSafeServiceBaseTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(SameUserSameTimeDifferentRequestsTest.class);

    @Test
    @SuppressWarnings("Duplicates")
    public void a() {
        transactionalDocumentSafeService.createUser(userIDAuth);

        DSDocument document1 = createDocument("file1");
        DSDocument document2 = createDocument("file2");

        {
            LOGGER.debug("user1 starts TX");
            transactionalDocumentSafeService.beginTransaction(userIDAuth);

            LOGGER.debug("user1 creates " + document1.getDocumentFQN());
            transactionalDocumentSafeService.txStoreDocument(userIDAuth, document1);

            LOGGER.debug("user1 ends TX");
            transactionalDocumentSafeService.endTransaction(userIDAuth);
        }
        {
            LOGGER.debug("user1 starts TX");
            transactionalDocumentSafeService.beginTransaction(userIDAuth);

            requestMemoryContext.switchToUser(2);
            LOGGER.debug("user1 starts another TX");
            transactionalDocumentSafeService.beginTransaction(userIDAuth);

            LOGGER.debug("user1 creates " + document2.getDocumentFQN());
            transactionalDocumentSafeService.txStoreDocument(userIDAuth, document2);

            requestMemoryContext.switchToUser(1);
            LOGGER.debug("user1 creates " + document2.getDocumentFQN());
            transactionalDocumentSafeService.txStoreDocument(userIDAuth, document2);

            LOGGER.debug("user1 ends TX");
            transactionalDocumentSafeService.endTransaction(userIDAuth);


            requestMemoryContext.switchToUser(2);
            LOGGER.debug("user1 ends TX");
     //       transactionalDocumentSafeService.endTransaction(userIDAuth);
        }


    }


}
