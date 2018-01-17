package org.adorsys.documentsafe.layer02service.impl;

import org.adorsys.documentsafe.layer00common.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.adorsys.documentsafe.layer02service.InterfaceBucketService;
import org.adorsys.documentsafe.layer02service.types.complextypes.BucketContent;
import org.adorsys.encobject.service.ContainerPersistence;

/**
 * Created by peter on 17.01.18 at 16:44.
 */
public class BucketService implements InterfaceBucketService {
    private ContainerPersistence containerPersistence;

    public BucketService(ContainerPersistence containerPersistence) {
        this.containerPersistence = containerPersistence;
    }

    @Override
    public void createBucket(BucketPath bucketPath) {
        try {
            containerPersistence.creteContainer(bucketPath.getObjectHandlePath());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public BucketContent readDocumentBucket(BucketPath bucketPath, boolean recursive) {
        return null;
    }
}
