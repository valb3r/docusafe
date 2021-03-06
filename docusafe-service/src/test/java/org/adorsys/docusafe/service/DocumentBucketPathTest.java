package org.adorsys.docusafe.service;

import org.adorsys.docusafe.service.types.complextypes.DocumentBucketPath;
import org.adorsys.encobject.complextypes.BucketDirectory;
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
        

        DocumentBucketPath bp=new DocumentBucketPath("aaa/b/c/d");
        BucketDirectory dd = bp.getBucketDirectory();
        Assert.assertEquals("dd container", "aaa", dd.getObjectHandle().getContainer());
        Assert.assertEquals("dd name     ", "b/c", dd.getObjectHandle().getName());
    }

}
