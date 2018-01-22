package org.adorsys.documentsafe.layer03business;

import org.adorsys.documentsafe.layer02service.types.DocumentID;
import org.adorsys.documentsafe.layer03business.types.DocumentFQN;
import org.adorsys.documentsafe.layer03business.types.RelativeBucketPath;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 20.01.18 at 07:47.
 */
public class DocumentFQNTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentFQNTest.class);

    @Test
    public void test1() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        DocumentFQN documentFQN = new DocumentFQN("a/b/c.affe");
        Assert.assertEquals(new RelativeBucketPath("a/b"), documentFQN.getRelativeBucketPath());
        Assert.assertEquals(new DocumentID("c.affe"), documentFQN.getDocumentID());
    }
    @Test
    public void test2() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        DocumentFQN documentFQN = new DocumentFQN("a/b/c with space.affe");
        Assert.assertEquals(new RelativeBucketPath("a/b"), documentFQN.getRelativeBucketPath());
        Assert.assertEquals(new DocumentID("c with space.affe"), documentFQN.getDocumentID());
    }
    @Test
    public void test3() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        DocumentFQN documentFQN = new DocumentFQN("c with space.affe");
        Assert.assertEquals(new RelativeBucketPath(""), documentFQN.getRelativeBucketPath());
        Assert.assertEquals(new DocumentID("c with space.affe"), documentFQN.getDocumentID());
    }
}
