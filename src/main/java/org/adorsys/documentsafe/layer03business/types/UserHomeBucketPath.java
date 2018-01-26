package org.adorsys.documentsafe.layer03business.types;

import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;

/**
 * Created by peter on 19.01.18 at 16:07.
 */
public class UserHomeBucketPath extends BucketPath {
    public UserHomeBucketPath(String path) {
        super(path);
    }

    public UserHomeBucketPath(BucketPath bucketPath) {
        super(bucketPath);
    }
}
