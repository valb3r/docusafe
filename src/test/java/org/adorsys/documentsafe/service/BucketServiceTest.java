package org.adorsys.documentsafe.service;

import org.adorsys.documentsafe.layer01persistence.ExtendedBlobStoreConnection;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.adorsys.documentsafe.layer02service.InterfaceBucketService;
import org.adorsys.documentsafe.layer02service.impl.BucketService;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.utils.TestFsBlobStoreFactory;

/**
 * Created by peter on 17.01.18 at 16:51.
 */
public class BucketServiceTest {
    private static InterfaceBucketService bucketService;
    public static void beforeClass() {
        TestFsBlobStoreFactory storeContextFactory = new TestFsBlobStoreFactory();
        ContainerPersistence containerPersistence = new ContainerPersistence(new ExtendedBlobStoreConnection(storeContextFactory));
        bucketService = new BucketService(containerPersistence);
    }

    public static void afterClass() {
    }

    public void createBucket(BucketPath bucketPath) {
        bucketService.createBucket(bucketPath);
    }

}
