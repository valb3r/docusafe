package org.adorsys.documentsafe.layer02service;

import org.adorsys.documentsafe.layer01persistence.types.BucketName;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by peter on 20.01.18 at 08:09.
 */
public class BucketPathTest {
    @Test
    public void test1() {
        BucketPath bp0 = new BucketPath("a/b/c");
        BucketPath bp1 = new BucketPath("d/e/f");
        BucketPath bp2 = bp0.append(bp1);
        Assert.assertEquals(new BucketPath("a/b/c"), bp0);
        Assert.assertEquals(new BucketPath("d/e/f"), bp1);
        Assert.assertEquals(new BucketPath("a/b/c/d/e/f"), bp2);
    }
}
