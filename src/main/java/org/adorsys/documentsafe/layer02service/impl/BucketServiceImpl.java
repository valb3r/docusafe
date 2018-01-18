package org.adorsys.documentsafe.layer02service.impl;

import org.adorsys.documentsafe.layer00common.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer01persistence.ExtendedBlobStoreConnection;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.adorsys.documentsafe.layer02service.BucketService;
import org.adorsys.documentsafe.layer02service.types.complextypes.BucketContent;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.adorsys.encobject.service.ContainerPersistence;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 17.01.18 at 16:44.
 */
public class BucketServiceImpl implements BucketService {
    private final static Logger LOGGER = LoggerFactory.getLogger(BucketServiceImpl.class);
    private ContainerPersistence containerPersistence;
    private ExtendedBlobStoreConnection extendedBlobStoreConnection;

    public BucketServiceImpl(BlobStoreContextFactory factory) {
        this.extendedBlobStoreConnection = new ExtendedBlobStoreConnection(factory);
        this.containerPersistence = new ContainerPersistence(this.extendedBlobStoreConnection);
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
        return new BucketContent(bucketPath, extendedBlobStoreConnection.list(bucketPath, recursive));
    }
}
