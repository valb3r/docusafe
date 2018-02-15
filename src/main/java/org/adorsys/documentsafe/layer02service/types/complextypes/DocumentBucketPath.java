package org.adorsys.documentsafe.layer02service.types.complextypes;

import org.adorsys.encobject.complextypes.BucketPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by peter on 06.01.18.
 */
public class DocumentBucketPath extends BucketPath {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentBucketPath.class);

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
