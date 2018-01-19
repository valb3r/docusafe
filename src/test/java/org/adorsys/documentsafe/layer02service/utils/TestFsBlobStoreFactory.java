package org.adorsys.documentsafe.layer02service.utils;

import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.filesystem.reference.FilesystemConstants;

import java.io.File;
import java.util.Properties;

public class TestFsBlobStoreFactory implements BlobStoreContextFactory {

	private static final String STORE_BASEDIR = "./target";
	private static final String DEFAULT_SUBDIR = "filesystemstorage";
	private String storeBasedir;

	Properties properties = new Properties();

	public TestFsBlobStoreFactory(){
		this(DEFAULT_SUBDIR);
	}

	public TestFsBlobStoreFactory(String defaultSubDir){
		this.storeBasedir = STORE_BASEDIR + "/" + defaultSubDir;
		properties.setProperty(FilesystemConstants.PROPERTY_BASEDIR, storeBasedir);
	}

	public boolean existsOnFs(String container, String name){
		File file = new File(storeBasedir + "/" + container + "/" + name );
		return file.exists();
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
