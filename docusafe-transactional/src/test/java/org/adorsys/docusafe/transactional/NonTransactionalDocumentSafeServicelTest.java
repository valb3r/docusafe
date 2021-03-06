package org.adorsys.docusafe.transactional;

import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 13.06.18 at 19:42.
 */
public class NonTransactionalDocumentSafeServicelTest extends TransactionalDocumentSafeServiceBaseTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(NonTransactionalDocumentSafeServicelTest.class);

    @Test
    public void testAllNonTransactional() {
        nonTransactionalDocumentSafeService.createUser(userIDAuth);
        Assert.assertTrue(nonTransactionalDocumentSafeService.userExists(userIDAuth.getUserID()));
        BucketContentFQN bucketContentFQN = nonTransactionalDocumentSafeService.nonTxListInbox(userIDAuth);
        Assert.assertEquals(0, bucketContentFQN.getFiles().size());
        Assert.assertEquals(0, bucketContentFQN.getDirectories().size());
    }

}
