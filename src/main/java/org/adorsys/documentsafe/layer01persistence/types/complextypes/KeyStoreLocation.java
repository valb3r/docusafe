package org.adorsys.documentsafe.layer01persistence.types.complextypes;

import org.adorsys.documentsafe.layer01persistence.LocationInterface;
import org.adorsys.documentsafe.layer01persistence.types.KeyStoreID;
import org.adorsys.documentsafe.layer01persistence.types.KeyStoreType;
import org.adorsys.encobject.domain.ObjectHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 29.12.2017 at 14:11:52.
 */
// TODO müsste auch komplett auf BucketPath umgestellt werden, so wie DocumentBucketPath
public class KeyStoreLocation implements LocationInterface {
	private final static Logger LOGGER = LoggerFactory.getLogger(KeyStoreLocation.class);
	public static final String FILE_EXTENSION_SEPARATOR = ".";

	private final KeyStoreBucketPath keyStoreBucketPath;
	private final KeyStoreID keyStoreID;
	private final KeyStoreType keyStoreType;

	public KeyStoreLocation(KeyStoreBucketPath keyStoreBucketPath, KeyStoreID keyStoreID, KeyStoreType keyStoreType) {
		this.keyStoreBucketPath = keyStoreBucketPath;
		this.keyStoreID = keyStoreID;
		this.keyStoreType = keyStoreType;
	}
	
	public KeyStoreBucketPath getKeyStoreBucketPath() {
		return keyStoreBucketPath;
	}

	public KeyStoreID getKeyStoreID() {
		return keyStoreID;
	}

	public KeyStoreType getKeyStoreType() {
		return keyStoreType;
	}

	public ObjectHandle getLocationHandle(){
		return getKeyStoreBucketPath().append(keyStoreID.getValue() + FILE_EXTENSION_SEPARATOR + keyStoreType.getValue()).getObjectHandle();
	}

	@Override
	public String toString() {
		return "KeyStoreLocation{" +
				keyStoreBucketPath +
				", " + keyStoreID +
				", " + keyStoreType +
				'}';
	}
}
