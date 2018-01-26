package org.adorsys.documentsafe.layer02service;

import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentBucketPath;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentDirectory;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 26.01.18 at 11:59.
 */
public class DocumentBucketPathTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentBucketPathTest.class);
    @Test
    public void test1() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        DocumentBucketPath bp=new DocumentBucketPath("a/b/c/d");
        DocumentDirectory dd = bp.getDocumentDirectory();
        Assert.assertEquals("dd container", "a", dd.getObjectHandle().getContainer());
        Assert.assertEquals("dd name     ", "b/c", dd.getObjectHandle().getName());
    }

}
