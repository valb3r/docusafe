package org.adorsys.documentsafe.layer02service;

import org.adorsys.documentsafe.layer01persistence.types.KeyStoreBucketName;
import org.adorsys.documentsafe.layer02service.types.DocumentBucketName;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentBucketContent;

/**
 * Created by peter on 15.01.18.
 */
public interface BucketService {
    void createDocumentBucket(DocumentBucketName documentBucketName);
    void createKeyStoreBucket(KeyStoreBucketName keyStoreBucketName);
    DocumentBucketContent readDocumentBucket(DocumentBucketName documentBucketName, boolean recursive);
}
