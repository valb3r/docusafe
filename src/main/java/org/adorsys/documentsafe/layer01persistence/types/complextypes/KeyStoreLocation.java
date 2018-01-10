package org.adorsys.documentsafe.layer01persistence.types.complextypes;

import org.adorsys.documentsafe.layer01persistence.LocationInterface;
import org.adorsys.documentsafe.layer01persistence.types.KeyStoreBucketName;
import org.adorsys.documentsafe.layer01persistence.types.KeyStoreID;
import org.adorsys.documentsafe.layer01persistence.types.KeyStoreType;
import org.adorsys.encobject.domain.ObjectHandle;

/**
 * Created by peter on 29.12.2017 at 14:11:52.
 */
public class KeyStoreLocation implements LocationInterface {
	
	public static final String FILE_EXTENSION_SEPARATOR = ".";

	private final KeyStoreBucketName keyStoreBucketName;
	private final KeyStoreID keyStoreID;
	private final KeyStoreType keyStoreType;

	public KeyStoreLocation(KeyStoreBucketName keyStoreBucketName, KeyStoreID keyStoreID, KeyStoreType keyStoreType) {
		this.keyStoreBucketName = keyStoreBucketName;
		this.keyStoreID = keyStoreID;
		this.keyStoreType = keyStoreType;
	}
	
	public KeyStoreBucketName getKeyStoreBucketName() {
		return keyStoreBucketName;
	}

	public KeyStoreID getKeyStoreID() {
		return keyStoreID;
	}

	public KeyStoreType getKeyStoreType() {
		return keyStoreType;
	}

	public ObjectHandle getLocationHandle(){
		return new ObjectHandle(
				getKeyStoreBucketName().getValue(),
				keyStoreID.getValue() + FILE_EXTENSION_SEPARATOR + keyStoreType.getValue());
	}

	@Override
	public String toString() {
		return "KeyStoreLocation{" +
				"keyStoreBucketName=" + keyStoreBucketName +
				", keyStoreID=" + keyStoreID +
				", keyStoreType=" + keyStoreType +
				'}';
	}
}
