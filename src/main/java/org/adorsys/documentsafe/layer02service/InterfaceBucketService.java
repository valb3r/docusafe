package org.adorsys.documentsafe.layer02service;

import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.adorsys.documentsafe.layer02service.types.complextypes.BucketContent;

/**
 * Created by peter on 15.01.18.
 */
public interface InterfaceBucketService {
    void createBucket(BucketPath bucketPath);
    BucketContent readDocumentBucket(BucketPath bucketPath, boolean recursive);
}
