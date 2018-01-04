package org.adorsys.resource.server.persistence.basetypes;

import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.resource.server.basetypes.UserID;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by peter on 29.12.2017 at 14:11:52.
 */
public class KeyStoreName extends BaseTypeString {
	
	public static final String FILE_EXTENSION_SEPARATOR = ".";

	private BucketName bucketName;

	private KeyStoreID keyStoreID;
	
	private KeyStoreType keyStoreType;
	
	public KeyStoreName() {}

    public KeyStoreName(String value) {
        super(value);
        fromString(value);
    }

	public KeyStoreName(BucketName bucketName, KeyStoreID keyStoreID, KeyStoreType keyStoreType) {
		super(toString(bucketName, keyStoreID, keyStoreType));
		this.bucketName = bucketName;
		this.keyStoreID = keyStoreID;
		this.keyStoreType = keyStoreType;
	}

	private static String toString(BucketName bucketName, KeyStoreID keyStoreID, KeyStoreType keyStoreType){
		return toFileName(keyStoreID, keyStoreType) + BucketName.BUCKET_SEPARATOR + bucketName.getValue();
	}
	
	private static String toFileName(KeyStoreID keyStoreID, KeyStoreType keyStoreType) {
		return keyStoreID.getValue() + FILE_EXTENSION_SEPARATOR + keyStoreType.getValue();
	}
	
	private void fromString(String storeFQN){ // Fuly qualifierd Name
		bucketName = new BucketName(StringUtils.substringAfterLast(storeFQN, BucketName.BUCKET_SEPARATOR));
		String storeName = StringUtils.substringBeforeLast(storeFQN, BucketName.BUCKET_SEPARATOR);
		keyStoreType = new KeyStoreType(StringUtils.substringAfterLast(storeName, FILE_EXTENSION_SEPARATOR));
		keyStoreID = new KeyStoreID(StringUtils.substringBeforeLast(storeName, FILE_EXTENSION_SEPARATOR));
	}

	public BucketName getBucketName() {
		return bucketName;
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
		return new ObjectHandle(getBucketName().getValue(), toFileName());
	}

    public static KeyStoreName findUserKeyStoreName(BucketName bucketName, UserID userId){
    	return new KeyStoreName(bucketName, new KeyStoreID(userId.getValue()), new KeyStoreType("UBER"));
    }
}
