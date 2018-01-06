package org.adorsys.resource.server.persistence.complextypes;

import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.resource.server.basetypes.DocumentKeyID;

/**
 * Created by peter on 23.12.2017 at 18:33:14.
 * 
 * @TODO: Add following fields:
 * - GuardKeyID ??
 */
public class DocumentGuardLocation {
	private static final long serialVersionUID = -3042461057378981231L;

	
	public static final String GUARD_NAME_COMPONENT_SEPARATOR = ".";
	
	private DocumentKeyID documentKeyID;
	private KeyStoreLocation keyStoreLocation;

	/*
	public DocumentGuardLocation() {}


	public DocumentGuardLocation(String value) {
		super(value);
		fromString(value);
	}
*/
	public DocumentGuardLocation(KeyStoreLocation keyStoreLocation, DocumentKeyID documentKeyID) {
		this.documentKeyID = documentKeyID;
		this.keyStoreLocation = keyStoreLocation;
	}

	public DocumentKeyID getDocumentKeyID() {
		return documentKeyID;
	}

	public KeyStoreLocation getKeyStoreLocation() {
		return keyStoreLocation;
	}

	public ObjectHandle getLocationHandle(){
		return new ObjectHandle(keyStoreLocation.getKeyStoreBucketName().getValue(), toFileName());
	}

	public String toFileName() {
		return toFileName(keyStoreLocation, documentKeyID);
	}

	public static String toFileName(KeyStoreLocation keyStoreLocation, DocumentKeyID documentKeyID) {
		return keyStoreLocation.toFileName() + GUARD_NAME_COMPONENT_SEPARATOR + documentKeyID.getValue() ;
	}

	/*

	private static String toString(KeyStoreLocation keyStoreLocation, DocumentKeyID documentKeyID){
		return toFileName(keyStoreLocation, documentKeyID) + DocumentGuardBucketName.BUCKET_SEPARATOR + keyStoreLocation.getKeyStoreBucketName().getValue();
	}


	private void fromString(String guardFQN){
		
		String bucketNameStr = StringUtils.substringAfterLast(guardFQN, DocumentGuardBucketName.BUCKET_SEPARATOR);
		
		String documentGuardfileName = StringUtils.substringBeforeLast(guardFQN, DocumentGuardBucketName.BUCKET_SEPARATOR);
		String documentKeyIDName = StringUtils.substringAfterLast(documentGuardfileName, GUARD_NAME_COMPONENT_SEPARATOR);
		String keyStoreFileName = StringUtils.substringBeforeLast(documentGuardfileName, GUARD_NAME_COMPONENT_SEPARATOR);
		
		// TOdo Peter refactor
		String keyStoreType = StringUtils.substringAfterLast(keyStoreFileName, KeyStoreLocation.FILE_EXTENSION_SEPARATOR);
		String keyStoreId = StringUtils.substringBeforeLast(keyStoreFileName, KeyStoreLocation.FILE_EXTENSION_SEPARATOR);
    	
		keyStoreLocation = new KeyStoreLocation(new KeyStoreBucketName(bucketNameStr), new KeyStoreID(keyStoreId), new KeyStoreType(keyStoreType));
		documentKeyID = new DocumentKeyID(documentKeyIDName);
	}
*/

	@Override
	public String toString() {
		return "DocumentGuardLocation{" +
				"documentKeyID=" + documentKeyID +
				", keyStoreLocation=" + keyStoreLocation +
				'}';
	}
}
