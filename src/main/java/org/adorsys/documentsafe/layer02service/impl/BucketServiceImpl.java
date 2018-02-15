package org.adorsys.documentsafe.layer02service.impl;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer02service.BucketService;
import org.adorsys.documentsafe.layer02service.types.PlainFileContent;
import org.adorsys.documentsafe.layer02service.types.complextypes.BucketContent;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.service.ExtendedStoreConnection;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 17.01.18 at 16:44.
 */
public class BucketServiceImpl implements BucketService {
    private final static Logger LOGGER = LoggerFactory.getLogger(BucketServiceImpl.class);
    private ContainerPersistence containerPersistence;
    private ExtendedStoreConnection extendedStoreConnection;

    public BucketServiceImpl(ExtendedStoreConnection extendedStoreConnection) {
        this.extendedStoreConnection = extendedStoreConnection;
        this.containerPersistence = new ContainerPersistence(this.extendedStoreConnection);
    }

    @Override
    public void createBucket(BucketDirectory bucketDirectory) {
        try {
            containerPersistence.createContainer(bucketDirectory.getObjectHandle().getContainer());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public void destroyBucket(BucketDirectory bucketDirectory) {
        try {
            containerPersistence.deleteContainer(bucketDirectory.getObjectHandle().getContainer());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public BucketContent readDocumentBucket(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag) {
        LOGGER.info("start read document bucket " + bucketDirectory);
        BucketContent bucketContent = new BucketContent(bucketDirectory, extendedStoreConnection.list(bucketDirectory, listRecursiveFlag));
        LOGGER.info("finished read document bucket " + bucketDirectory + " -> " + bucketContent.getOriginalContent().size());
        return bucketContent;
    }

    @Override
    public boolean bucketExists(BucketDirectory bucketDirectory) {
        LOGGER.info("start check bucket exsits " + bucketDirectory);
        boolean b = extendedStoreConnection.containerExists(bucketDirectory.getObjectHandle().getContainer());
        LOGGER.info("finished check bucket exsits " + bucketDirectory + " -> " + b);
        return b;
    }

    @Override
    public void createPlainFile(BucketPath bucketPath, PlainFileContent plainFileContent) {
        try {
            LOGGER.info("start create plain file " + bucketPath);
            extendedStoreConnection.putBlob(bucketPath, plainFileContent.getValue());
            LOGGER.info("finished create plain file " + bucketPath);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public void deletePlainFile(BucketPath bucketPath) {
        try {
            LOGGER.info("start delete plain file " + bucketPath);
            extendedStoreConnection.removeBlob(bucketPath);
            LOGGER.info("finished delete plain file " + bucketPath);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public PlainFileContent readPlainFile(BucketPath bucketPath) {
        try {
            LOGGER.info("start read plain file " + bucketPath);
            PlainFileContent plainFileContent = new PlainFileContent(extendedStoreConnection.getBlob(bucketPath).getData());
            LOGGER.info("finished read plain file " + bucketPath);
            return plainFileContent;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public boolean fileExists(BucketPath bucketPath) {
        LOGGER.info("start file exists " + bucketPath);
        boolean blobExists = extendedStoreConnection.blobExists(bucketPath);
        LOGGER.info("finished file exists " + bucketPath);
        return blobExists;
    }

}
