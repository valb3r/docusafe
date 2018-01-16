package org.adorsys.documentsafe.layer02service;

import org.adorsys.documentsafe.layer01persistence.types.complextypes.KeyStoreBucketPath;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentBucketPath;
import org.adorsys.documentsafe.layer02service.types.complextypes.BucketContent;

/**
 * Created by peter on 15.01.18.
 */
public interface BucketService {
    void createBucket(KeyStoreBucketPath keyStoreBucketPath);
    BucketContent readDocumentBucket(DocumentBucketPath documentBucketPath, boolean recursive);
}
