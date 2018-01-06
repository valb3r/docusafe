package org.adorsys.resource.server.persistence.basetypes;

import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.resource.server.basetypes.UserID;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by peter on 29.12.2017 at 14:11:52.
 */
public class KeyStoreName extends BaseTypeString {
	
	public static final String FILE_EXTENSION_SEPARATOR = ".";

	private KeyStoreBucketName keyStoreBucketName;
	private KeyStoreID keyStoreID;
	private KeyStoreType keyStoreType;
	
	public KeyStoreName() {}

    public KeyStoreName(String value) {
        super(value);
        fromString(value);
    }

	public KeyStoreName(KeyStoreBucketName keyStoreBucketName, KeyStoreID keyStoreID, KeyStoreType keyStoreType) {
		super(toString(keyStoreBucketName, keyStoreID, keyStoreType));
		this.keyStoreBucketName = keyStoreBucketName;
		this.keyStoreID = keyStoreID;
		this.keyStoreType = keyStoreType;
	}
	
	private static String toString(KeyStoreBucketName bucketName, KeyStoreID keyStoreID, KeyStoreType keyStoreType){
		return toFileName(keyStoreID, keyStoreType) + KeyStoreBucketName.BUCKET_SEPARATOR + bucketName.getValue();
	}
	
	private static String toFileName(KeyStoreID keyStoreID, KeyStoreType keyStoreType) {
		return keyStoreID.getValue() + FILE_EXTENSION_SEPARATOR + keyStoreType.getValue();
	}
	
	private void fromString(String storeFQN){ // Fuly qualifierd Name
		keyStoreBucketName = new KeyStoreBucketName(StringUtils.substringAfterLast(storeFQN, KeyStoreBucketName.BUCKET_SEPARATOR));
		String storeName = StringUtils.substringBeforeLast(storeFQN, KeyStoreBucketName.BUCKET_SEPARATOR);
		keyStoreType = new KeyStoreType(StringUtils.substringAfterLast(storeName, FILE_EXTENSION_SEPARATOR));
		keyStoreID = new KeyStoreID(StringUtils.substringBeforeLast(storeName, FILE_EXTENSION_SEPARATOR));
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

	public String toFileName() {
		return toFileName(keyStoreID, keyStoreType);
	}
	
	public ObjectHandle toLocation(){
		return new ObjectHandle(getKeyStoreBucketName().getValue(), toFileName());
	}

    public static KeyStoreName findUserKeyStoreName(KeyStoreBucketName keyStoreBucketName, UserID userId){
    	return new KeyStoreName(keyStoreBucketName, new KeyStoreID(userId.getValue()), new KeyStoreType("UBER"));
    }

	@Override
	public String toString() {
		return "KeyStoreName{" +
				"keyStoreBucketName=" + keyStoreBucketName +
				", keyStoreID=" + keyStoreID +
				", keyStoreType=" + keyStoreType +
				", value=" + getValue() +
				'}';
	}
}
