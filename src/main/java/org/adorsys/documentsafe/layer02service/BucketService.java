package org.adorsys.documentsafe.layer02service;

import org.adorsys.documentsafe.layer02service.types.PlainFileContent;
import org.adorsys.documentsafe.layer02service.types.complextypes.BucketContent;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.types.ListRecursiveFlag;

/**
 * Created by peter on 15.01.18.
 */
public interface BucketService {
    void createBucket(BucketPath bucketPath);
    void destroyBucket(BucketPath bucketPath);
    BucketContent readDocumentBucket(BucketPath bucketPath, ListRecursiveFlag listRecursiveFlag);
    boolean bucketExists(BucketPath bucketPath);
    void createPlainFile(BucketPath bucketPath, PlainFileContent plainFileContent);
    void deletePlainFile(BucketPath bucketPath);
    PlainFileContent readPlainFile(BucketPath bucketPath);
    boolean existsFile(BucketPath bucketPath);
}
