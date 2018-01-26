package org.adorsys.documentsafe.layer01persistence;

import org.adorsys.documentsafe.layer01persistence.types.BucketName;
import org.adorsys.documentsafe.layer01persistence.types.ListRecursiveFlag;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.service.BlobStoreConnection;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.adorsys.encobject.service.ContainerExistsException;
import org.adorsys.encobject.service.UnknownContainerException;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 16.01.18.
 */
public class ExtendedBlobStoreConnection extends BlobStoreConnection {
    private final static Logger LOGGER = LoggerFactory.getLogger(ExtendedBlobStoreConnection.class);
    private final BlobStoreContextFactory factory;

    public ExtendedBlobStoreConnection(BlobStoreContextFactory blobStoreContextFactory) {
        super(blobStoreContextFactory);
        this.factory = blobStoreContextFactory;
    }

    /**
     * Wenn der Container bereits exisitiert, wird das ignoriert.
     */
    @Override
    public void createContainer(String container) throws ContainerExistsException {
        BlobStoreContext blobStoreContext = this.factory.alocate();

        ObjectHandle objectHandle = new BucketPath(container).getObjectHandle();
        try {
            BlobStore blobStore = blobStoreContext.getBlobStore();
            if (!blobStore.containerExists(container)) {
                blobStoreContext.getBlobStore().createContainerInLocation(null, objectHandle.getContainer());
            }
        } finally {
            this.factory.dispose(blobStoreContext);
        }

    }

    @Override
    public void deleteContainer(String container) throws UnknownContainerException {
        BlobStoreContext blobStoreContext = this.factory.alocate();
        ObjectHandle objectHandle = new BucketPath(container).getObjectHandle();
        try {
            blobStoreContext.getBlobStore().deleteContainer(objectHandle.getContainer());
        } finally {
            this.factory.dispose(blobStoreContext);
        }
    }

    @Override
    public boolean containerExists(String container) {
        BlobStoreContext blobStoreContext = this.factory.alocate();
        ObjectHandle objectHandle = new BucketPath(container).getObjectHandle();

        boolean bucketExists = false;
        try {
            BlobStore blobStore = blobStoreContext.getBlobStore();
            bucketExists = blobStore.containerExists(objectHandle.getContainer());
        } finally {
            this.factory.dispose(blobStoreContext);
        }

        return bucketExists;
    }

    /**
     * Achtung, gehört nicht zum derzeitigen Interface
     *
     * @return
     */
    public boolean blobExists(ObjectHandle location) {
        BlobStoreContext blobStoreContext = this.factory.alocate();
        try {
            BlobStore blobStore = blobStoreContext.getBlobStore();
            LOGGER.debug("container:" + location.getContainer());
            LOGGER.debug("name     :" + location.getName());
            if (location.getContainer() == null) {
                LOGGER.warn("dont know how to check if container is null");
                return false;
            }
            return blobStore.blobExists(location.getContainer() != null ? location.getContainer() : "",
                    location.getName() != null ? location.getName() : "");
        } finally {
            this.factory.dispose(blobStoreContext);
        }
    }

    public PageSet<? extends StorageMetadata> list(BucketPath bucketPath, ListRecursiveFlag listRecursiveFlag) {
        BlobStoreContext blobStoreContext = this.factory.alocate();
        try {
            BlobStore blobStore = blobStoreContext.getBlobStore();
            ListContainerOptions listContainerOptions = new ListContainerOptions();
            if (listRecursiveFlag == ListRecursiveFlag.TRUE) {
                listContainerOptions.recursive();
            }
            ObjectHandle objectHandle = bucketPath.getObjectHandle();
            if (objectHandle.getName() != null) {
                String prefix = objectHandle.getName() + BucketPath.BUCKET_SEPARATOR;
                listContainerOptions.prefix(prefix);
                if (listRecursiveFlag == ListRecursiveFlag.FALSE) {
                    listContainerOptions.delimiter(BucketName.BUCKET_SEPARATOR);
                }
            }

            LOGGER.debug("list container:" + objectHandle.getContainer() + " prefix:" + listContainerOptions.getPrefix() + " del:" + listContainerOptions.getDelimiter());
            return blobStore.list(objectHandle.getContainer(), listContainerOptions);
        } finally

        {
            this.factory.dispose(blobStoreContext);
        }
    }

}
