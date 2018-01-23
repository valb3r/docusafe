package org.adorsys.documentsafe.layer04rest.impl;

import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.filesystem.reference.FilesystemConstants;

import java.util.Properties;

/**
 * Created by peter on 22.01.18 at 20:40.
 */
public class FileSystemBlobStoreFactory implements BlobStoreContextFactory {

    private static final String STORE_BASEDIR = "./target";
    private static final String DEFAULT_SUBDIR = "filesystemstorage";
    private String storeBasedir;

    Properties properties = new Properties();

    public FileSystemBlobStoreFactory(){
        this(DEFAULT_SUBDIR);
    }

    public FileSystemBlobStoreFactory(String defaultSubDir){
        this.storeBasedir = STORE_BASEDIR + "/" + defaultSubDir;
        properties.setProperty(FilesystemConstants.PROPERTY_BASEDIR, storeBasedir);
    }

    @Override
    public BlobStoreContext alocate() {
        return ContextBuilder.newBuilder("filesystem")
                .overrides(properties)
                .buildView(BlobStoreContext.class);
    }

    @Override
    public void dispose(BlobStoreContext blobStoreContext) {
        blobStoreContext.close();
    }
}
