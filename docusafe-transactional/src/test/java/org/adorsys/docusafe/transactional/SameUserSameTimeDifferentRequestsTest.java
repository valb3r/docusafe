package org.adorsys.docusafe.transactional;

import com.googlecode.catchexception.CatchException;
import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.docusafe.business.impl.DocumentSafeServiceImpl;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.transactional.impl.TransactionalDocumentSafeServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 07.03.19 11:22.
 */
public class SameUserSameTimeDifferentRequestsTest extends TransactionalDocumentSafeServiceBaseTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(SameUserSameTimeDifferentRequestsTest.class);

    SimpleRequestMemoryContextImpl secondRequestMemoryContext = new SimpleRequestMemoryContextImpl();
    TransactionalDocumentSafeService secondTransactionalDocumentSafeService = new TransactionalDocumentSafeServiceImpl(secondRequestMemoryContext, new DocumentSafeServiceImpl(ExtendedStoreConnectionFactory.get()));

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

            secondRequestMemoryContext
            LOGGER.debug("user1 in a new context starts another TX");
            secondTransactionalDocumentSafeService.beginTransaction(userIDAuth);

            LOGGER.debug("user2 creates " + document2.getDocumentFQN());
            secondTransactionalDocumentSafeService.txStoreDocument(userIDAuth, document2);

            LOGGER.debug("user1 creates " + document2.getDocumentFQN());
            transactionalDocumentSafeService.txStoreDocument(userIDAuth, document2);

            LOGGER.debug("user1 ends TX");
            transactionalDocumentSafeService.endTransaction(userIDAuth);

            LOGGER.debug("user1 in the new context ends TX");
            CatchException.catchException(() ->secondTransactionalDocumentSafeService.endTransaction(userIDAuth));
            Assert.assertNotNull(CatchException.caughtException());
        }


    }


}
