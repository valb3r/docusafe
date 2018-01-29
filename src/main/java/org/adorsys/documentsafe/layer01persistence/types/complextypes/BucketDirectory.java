package org.adorsys.documentsafe.layer01persistence.types.complextypes;

/**
 * Created by peter on 29.01.18 at 14:40.
 */
public class BucketDirectory extends BucketPath {
    public BucketDirectory(String path) {
        super(path);
    }

    public BucketDirectory(BucketPath bucketPath) {
        super(bucketPath);
    }
}
