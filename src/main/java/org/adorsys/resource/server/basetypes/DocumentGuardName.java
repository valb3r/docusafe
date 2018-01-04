package org.adorsys.resource.server.basetypes;

import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.resource.server.persistence.basetypes.BaseTypeString;
import org.adorsys.resource.server.persistence.basetypes.BucketName;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreID;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreName;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreType;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by peter on 23.12.2017 at 18:33:14.
 * 
 * @TODO: Add following fields:
 * - GuardKeyID ??
 */
public class DocumentGuardName extends BaseTypeString {
	private static final long serialVersionUID = -3042461057378981231L;

	
	public static final String GUARD_NAME_COMPONENT_SEPARATOR = ".";
	
	private DocumentKeyID documentKeyID;
	private KeyStoreName keyStoreName;

	public DocumentGuardName() {}


	public DocumentGuardName(String value) {
		super(value);
		fromString(value);
	}

	public DocumentGuardName(KeyStoreName keyStoreName, DocumentKeyID documentKeyID) {
		super(toString(keyStoreName, documentKeyID));
		this.documentKeyID = documentKeyID;
		this.keyStoreName = keyStoreName;
	}

	public DocumentKeyID getDocumentKeyID() {
		return documentKeyID;
	}

	public KeyStoreName getKeyStoreName() {
		return keyStoreName;
	}

	public ObjectHandle toLocation(){
		return new ObjectHandle(keyStoreName.getBucketName().getValue(), toFileName());
	}

	private String toFileName() {
		return toFileName(keyStoreName, documentKeyID);
	}


	private static String toString(KeyStoreName keyStoreName, DocumentKeyID documentKeyID){
		return toFileName(keyStoreName, documentKeyID) + BucketName.BUCKET_SEPARATOR + keyStoreName.getBucketName().getValue();
	}
	
	private static String toFileName(KeyStoreName keyStoreName, DocumentKeyID documentKeyID) {
		return keyStoreName.toFileName() + GUARD_NAME_COMPONENT_SEPARATOR + documentKeyID.getValue() ;
	}


	private void fromString(String guardFQN){
		
		String bucketNameStr = StringUtils.substringAfterLast(guardFQN, BucketName.BUCKET_SEPARATOR);
		
		String documentGuardfileName = StringUtils.substringBeforeLast(guardFQN, BucketName.BUCKET_SEPARATOR);
		String documentKeyIDName = StringUtils.substringAfterLast(documentGuardfileName, GUARD_NAME_COMPONENT_SEPARATOR);
		String keyStoreFileName = StringUtils.substringBeforeLast(documentGuardfileName, GUARD_NAME_COMPONENT_SEPARATOR);
		
		// TOdo Peter refactor
		String keyStoreType = StringUtils.substringAfterLast(keyStoreFileName, KeyStoreName.FILE_EXTENSION_SEPARATOR);
		String keyStoreId = StringUtils.substringBeforeLast(keyStoreFileName, KeyStoreName.FILE_EXTENSION_SEPARATOR);
    	
		keyStoreName = new KeyStoreName(new BucketName(bucketNameStr), new KeyStoreID(keyStoreId), new KeyStoreType(keyStoreType));
		documentKeyID = new DocumentKeyID(documentKeyIDName);
	}

	@Override
	public String toString() {
		return "DocumentGuardName{" +
				", documentKeyID=" + documentKeyID +
				", keyStoreName=" + keyStoreName +
				'}';
	}
}
