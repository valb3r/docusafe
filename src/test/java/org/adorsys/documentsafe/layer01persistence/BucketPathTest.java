package org.adorsys.documentsafe.layer01persistence;

import org.adorsys.documentsafe.layer01persistence.types.BucketName;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by peter on 16.01.18.
 */
public class BucketPathTest {
    @Test
    public void test1() {
        BucketPath bp=new BucketPath("a/b/c/d");
        Assert.assertEquals("bucket", bp.getFirstBucket(), new BucketName("a"));
        Assert.assertEquals("sub bucket", bp.getSubBuckets(), "b/c/d/");
        Assert.assertEquals("full path", bp.getObjectHandlePath(), "a/b/c/d");
    }

    @Test
    public void test2() {
        BucketPath bp=new BucketPath("a");
        Assert.assertEquals("bucket", bp.getFirstBucket(), new BucketName("a"));
        Assert.assertEquals("sub bucket", bp.getSubBuckets(), "");
        Assert.assertEquals("full path", bp.getObjectHandlePath(), "a");
    }
}
