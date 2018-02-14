package org.adorsys.documentsafe.layer02service;

import org.adorsys.documentsafe.layer02service.types.PlainFileContent;
import org.adorsys.documentsafe.layer02service.types.complextypes.BucketContent;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.types.ListRecursiveFlag;

/**
 * Created by peter on 15.01.18.
 */
public interface BucketService {
    void createBucket(BucketDirectory bucketDirectory);
    void destroyBucket(BucketDirectory bucketDirectory);

    boolean bucketExists(BucketDirectory bucketDirectory);
    boolean fileExists(BucketPath bucketPath);

    void createPlainFile(BucketPath bucketPath, PlainFileContent plainFileContent);
    PlainFileContent readPlainFile(BucketPath bucketPath);
    void deletePlainFile(BucketPath bucketPath);

    BucketContent readDocumentBucket(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag);
}
