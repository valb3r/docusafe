package org.adorsys.documentsafe.layer01persistence;

import org.adorsys.encobject.service.BlobStoreConnection;
import org.adorsys.encobject.service.BlobStoreContextFactory;

/**
 * Created by peter on 16.01.18.
 */
public class ExtendedBlobStoreConnection extends BlobStoreConnection {
    public ExtendedBlobStoreConnection(BlobStoreContextFactory blobStoreContextFactory) {
        super(blobStoreContextFactory);
    }
}
