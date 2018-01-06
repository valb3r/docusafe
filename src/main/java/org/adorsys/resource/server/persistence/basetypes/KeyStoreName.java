package org.adorsys.resource.server.persistence.basetypes;

/**
 * Created by peter on 06.01.18.
 */
public class KeyStoreName extends BaseTypeString {
    public KeyStoreName(String value) {
        super(value);
    }

    	/*
	public KeyStoreLocation() {}
    public KeyStoreLocation(String value) {
        super(value);
        fromString(value);
    }
    */

    	/*
	private void fromString(String storeFQN){ // Fuly qualifierd Name
		keyStoreBucketName = new KeyStoreBucketName(StringUtils.substringAfterLast(storeFQN, KeyStoreBucketName.BUCKET_SEPARATOR));
		String storeName = StringUtils.substringBeforeLast(storeFQN, KeyStoreBucketName.BUCKET_SEPARATOR);
		keyStoreType = new KeyStoreType(StringUtils.substringAfterLast(storeName, FILE_EXTENSION_SEPARATOR));
		keyStoreID = new KeyStoreID(StringUtils.substringBeforeLast(storeName, FILE_EXTENSION_SEPARATOR));
	}
	*/
	/*
    public static KeyStoreLocation findUserKeyStoreName(KeyStoreBucketName keyStoreBucketName, UserID userId){
    	return new KeyStoreLocation(keyStoreBucketName, new KeyStoreID(userId.getValue()), new KeyStoreType("UBER"));
    }
    */

}
