package org.adorsys.documentsafe.service;

import org.adorsys.documentsafe.layer01persistence.ExtendedBlobStoreConnection;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.adorsys.documentsafe.layer02service.BucketService;
import org.adorsys.documentsafe.layer02service.impl.BucketServiceImpl;
import org.adorsys.encobject.utils.TestFsBlobStoreFactory;

/**
 * Created by peter on 17.01.18 at 16:51.
 */
public class BucketServiceTest {
    private static BucketService bucketService;
    public static void beforeClass() {
        TestFsBlobStoreFactory storeContextFactory = new TestFsBlobStoreFactory();
        bucketService = new BucketServiceImpl(new ExtendedBlobStoreConnection(storeContextFactory));
    }

    public static void afterClass() {
    }

    public void createBucket(BucketPath bucketPath) {
        bucketService.createBucket(bucketPath);
    }

    public void listBucket(BucketPath bucketPath, boolean recursive) {
        bucketService.readDocumentBucket(bucketPath, recursive);
    }

}
