package org.adorsys.docusafe.service.impl;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.docusafe.service.BucketService;
import org.adorsys.docusafe.service.types.BucketContent;
import org.adorsys.docusafe.service.types.PlainFileContent;
import org.adorsys.docusafe.service.types.complextypes.BucketContentImpl;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.service.api.ContainerPersistence;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.impl.ContainerPersistenceImpl;
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
        this.containerPersistence = new ContainerPersistenceImpl(this.extendedStoreConnection);
    }

    @Override
    public void createBucket(BucketDirectory bucketDirectory) {
        containerPersistence.createContainer(bucketDirectory);
    }

    @Override
    public void destroyBucket(BucketDirectory bucketDirectory) {
            containerPersistence.deleteContainer(bucketDirectory);
    }

    @Override
    public BucketContent readDocumentBucket(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag) {
        LOGGER.debug("start read document bucket " + bucketDirectory);
        BucketContentImpl bucketContent = new BucketContentImpl(bucketDirectory, extendedStoreConnection.list(bucketDirectory, listRecursiveFlag));
        LOGGER.debug("finished read document bucket " + bucketDirectory + " -> " + bucketContent.getContent().size());
        return bucketContent;
    }

    @Override
    public boolean bucketExists(BucketDirectory bucketDirectory) {
        LOGGER.debug("start check bucket exsits " + bucketDirectory);
        boolean b = extendedStoreConnection.containerExists(bucketDirectory);
        LOGGER.debug("finished check bucket exsits " + bucketDirectory + " -> " + b);
        return b;
    }

    @Override
    public void createPlainFile(BucketPath bucketPath, PlainFileContent plainFileContent) {
        try {
            LOGGER.debug("start create plain file " + bucketPath);
            extendedStoreConnection.putBlob(bucketPath, plainFileContent.getValue());
            LOGGER.debug("finished create plain file " + bucketPath);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public void deletePlainFile(BucketPath bucketPath) {
        try {
            LOGGER.debug("start delete plain file " + bucketPath);
            extendedStoreConnection.removeBlob(bucketPath);
            LOGGER.debug("finished delete plain file " + bucketPath);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public void deletePlainFolder(BucketDirectory bucketDirectory) {
        try {
            LOGGER.debug("start delete plain directory " + bucketDirectory);
            extendedStoreConnection.removeBlobFolder(bucketDirectory);
            LOGGER.debug("finished delete plain file " + bucketDirectory);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }

    @Override
    public PlainFileContent readPlainFile(BucketPath bucketPath) {
        try {
            LOGGER.debug("start read plain file " + bucketPath);
            PlainFileContent plainFileContent = new PlainFileContent(extendedStoreConnection.getBlob(bucketPath).getData());
            LOGGER.debug("finished read plain file " + bucketPath);
            return plainFileContent;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public boolean fileExists(BucketPath bucketPath) {
        LOGGER.debug("start file exists " + bucketPath);
        boolean blobExists = extendedStoreConnection.blobExists(bucketPath);
        LOGGER.debug("finished file exists " + bucketPath);
        return blobExists;
    }

}
