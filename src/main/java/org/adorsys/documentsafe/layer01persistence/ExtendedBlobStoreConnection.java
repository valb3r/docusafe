package org.adorsys.documentsafe.layer01persistence;

import org.adorsys.documentsafe.layer00common.exceptions.BaseException;
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
import org.jclouds.domain.Location;
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

    @Override
    public void createContainer(String container) throws ContainerExistsException {
        BlobStoreContext blobStoreContext = this.factory.alocate();

        try {
            BucketPath bp = new BucketPath(container);
            if (bp.getDepth() <= 1) {
                BlobStore blobStore = blobStoreContext.getBlobStore();
                if (blobStore.containerExists(container)) {
                    throw new ContainerExistsException(container);
                }
            }
            if (bp.getDepth() > 1) {
                blobStoreContext.getBlobStore().createContainerInLocation((Location) null, bp.getFirstBucket().getValue());
                // TODO nicht schön
                blobStoreContext.getBlobStore().createDirectory(bp.getFirstBucket().getValue(), bp.getSubBuckets());
            } else {
                blobStoreContext.getBlobStore().createContainerInLocation((Location) null, container);
            }
        } finally {
            this.factory.dispose(blobStoreContext);
        }

    }

    @Override
    public void deleteContainer(String container) throws UnknownContainerException {
        BlobStoreContext blobStoreContext = this.factory.alocate();
        try {
            BucketPath bucketPath = new BucketPath(container);
            if (bucketPath.getDepth() > 1) {
                throw new BaseException("NYI delete bucket with path " + bucketPath.toString());
            }
            BucketPath bp = new BucketPath(container);
            BlobStore blobStore = blobStoreContext.getBlobStore();
            blobStoreContext.getBlobStore().deleteContainer(bp.getObjectHandlePath());
        } finally {
            this.factory.dispose(blobStoreContext);
        }
    }

    @Override
    public boolean containerExists(String container) {
        BlobStoreContext blobStoreContext = this.factory.alocate();

        boolean bucketExists = false;
        try {
            BlobStore blobStore = blobStoreContext.getBlobStore();
            BucketPath bucketPath = new BucketPath(container);
            if (bucketPath.getDepth() > 1) {
                throw new BaseException("container exsits for bucket with path " + bucketPath.toString());
            }
            bucketExists = blobStore.containerExists(bucketPath.getObjectHandlePath());
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
            return blobStore.blobExists(location.getContainer(), location.getName());
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
            if (bucketPath.getDepth() > 1) {
                String prefix = bucketPath.getSubBuckets();
                LOGGER.info("set prefix to " + prefix);
                listContainerOptions.prefix(prefix);
                if (listRecursiveFlag == ListRecursiveFlag.FALSE) {
                    listContainerOptions.delimiter(BucketName.BUCKET_SEPARATOR);
                }
            }

            return blobStore.list(bucketPath.getFirstBucket().getValue(), listContainerOptions);
        } finally

        {
            this.factory.dispose(blobStoreContext);
        }
    }

}
