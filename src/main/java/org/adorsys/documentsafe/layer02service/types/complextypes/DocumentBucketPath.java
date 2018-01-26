package org.adorsys.documentsafe.layer02service.types.complextypes;

import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;


/**
 * Created by peter on 06.01.18.
 */
public class DocumentBucketPath extends BucketPath {
    public DocumentBucketPath() {
    }

    public DocumentBucketPath(String value) {
        super(value);
    }

    public DocumentBucketPath(BucketPath bucketPath) {
        super(bucketPath);
    }

    public DocumentBucketPath(String container, String path) {
        super(container, path);
    }
}
