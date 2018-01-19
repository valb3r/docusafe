package org.adorsys.documentsafe.layer02service;

import org.adorsys.documentsafe.layer01persistence.types.ListRecursiveFlag;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.adorsys.documentsafe.layer02service.types.PlainFileContent;
import org.adorsys.documentsafe.layer02service.types.PlainFileName;
import org.adorsys.documentsafe.layer02service.types.complextypes.BucketContent;

/**
 * Created by peter on 15.01.18.
 */
public interface BucketService {
    void createBucket(BucketPath bucketPath);
    BucketContent readDocumentBucket(BucketPath bucketPath, ListRecursiveFlag listRecursiveFlag);
    boolean bucketExists(BucketPath bucketPath);
    void createPlainFile(BucketPath bucketPath, PlainFileName plainFileName, PlainFileContent plainFileContent);
}
