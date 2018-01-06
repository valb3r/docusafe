package org.adorsys.resource.server.persistence.complextypes;

import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreBucketName;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreID;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreName;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreType;

/**
 * Created by peter on 29.12.2017 at 14:11:52.
 */
public class KeyStoreLocation {
	
	public static final String FILE_EXTENSION_SEPARATOR = ".";

	private final KeyStoreBucketName keyStoreBucketName;
	private final KeyStoreID keyStoreID;
	private final KeyStoreType keyStoreType;

	public KeyStoreLocation(KeyStoreBucketName keyStoreBucketName, KeyStoreID keyStoreID, KeyStoreType keyStoreType) {
		this.keyStoreBucketName = keyStoreBucketName;
		this.keyStoreID = keyStoreID;
		this.keyStoreType = keyStoreType;
	}
	
	private static String toString(KeyStoreBucketName bucketName, KeyStoreID keyStoreID, KeyStoreType keyStoreType){
		return toFileName(keyStoreID, keyStoreType) + KeyStoreBucketName.BUCKET_SEPARATOR + bucketName.getValue();
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

	public ObjectHandle toLocationHandle(){
		return new ObjectHandle(getKeyStoreBucketName().getValue(), toFileName());
	}

	public KeyStoreName getKeyStoreName() {
		return new KeyStoreName(toFileName());
	}

	public String toFileName() {
		return toFileName(keyStoreID, keyStoreType);
	}
	private static String toFileName(KeyStoreID keyStoreID, KeyStoreType keyStoreType) {
		return keyStoreID.getValue() + FILE_EXTENSION_SEPARATOR + keyStoreType.getValue();
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
