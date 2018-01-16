package org.adorsys.documentsafe.layer01persistence.types.complextypes;

import org.adorsys.documentsafe.layer01persistence.LocationInterface;
import org.adorsys.documentsafe.layer01persistence.types.KeyStoreID;
import org.adorsys.documentsafe.layer01persistence.types.KeyStoreType;
import org.adorsys.encobject.domain.ObjectHandle;

/**
 * Created by peter on 29.12.2017 at 14:11:52.
 */
public class KeyStoreLocation implements LocationInterface {
	
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
		return new ObjectHandle(
				getKeyStoreBucketPath().getObjectHandlePath(),
				keyStoreID.getValue() + FILE_EXTENSION_SEPARATOR + keyStoreType.getValue());
	}

	@Override
	public String toString() {
		return "KeyStoreLocation{" +
				"keyStoreBucketPath=" + keyStoreBucketPath +
				", keyStoreID=" + keyStoreID +
				", keyStoreType=" + keyStoreType +
				'}';
	}
}
