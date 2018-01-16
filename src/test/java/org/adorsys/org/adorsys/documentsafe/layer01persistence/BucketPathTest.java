package org.adorsys.org.adorsys.documentsafe.layer01persistence;

import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.junit.Test;

/**
 * Created by peter on 16.01.18.
 */
public class BucketPathTest {
    @Test
    public void test1() {
        BucketPath bp=new BucketPath("a/b/c/d");
        System.out.println(bp.getFirstBucket());
        System.out.println(bp.getSubBuckets());
    }
}
