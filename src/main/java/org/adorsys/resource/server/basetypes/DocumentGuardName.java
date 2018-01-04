package org.adorsys.resource.server.basetypes;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by peter on 23.12.2017 at 18:33:14.
 * 
 * @TODO: Add following fields:
 * - GuardKeyID ??
 */
public class DocumentGuardName extends BaseTypeString {
	private static final long serialVersionUID = -3042461057378981231L;

	/**
	 * Separator between guard name and containing bucket.
	 */
	public static final String BUCKET_SEPARATOR = "@";
	
	public static final String GUARD_NAME_COMPONENT_SEPARATOR = ".";
	
	private BucketName guardBucketName;
	private DocumentKeyID documentKeyID;
	private UserID userId;

	public DocumentGuardName() {}


	public DocumentGuardName(String value) {
		super(value);
		fromString(value);
	}

	public DocumentGuardName(BucketName guardBucketName, UserID userId, DocumentKeyID documentKeyID) {
		super(toGuardName(guardBucketName, userId, documentKeyID));
		this.documentKeyID = documentKeyID;
		this.userId = userId;
		this.guardBucketName = guardBucketName;
	}

	public DocumentKeyID getDocumentKeyID() {
		return documentKeyID;
	}

	public UserID getUserId() {
		return userId;
	}
	
	public BucketName getGuardBucketName() {
		return guardBucketName;
	}


	private static String toGuardName(BucketName guardBucketName, UserID userID, DocumentKeyID documentKeyID){
		return userID.getValue() + GUARD_NAME_COMPONENT_SEPARATOR + documentKeyID.getValue() + BUCKET_SEPARATOR + guardBucketName.getValue();
	}
	
	private static DocumentGuardName fromString(String guardFQN){
		String guardBucketName = StringUtils.substringAfterLast(guardFQN, BUCKET_SEPARATOR);
		String guardName = StringUtils.substringBeforeLast(guardFQN, BUCKET_SEPARATOR);
		String documentKeyIDName = StringUtils.substringAfterLast(guardName, GUARD_NAME_COMPONENT_SEPARATOR);
		String userIdName = StringUtils.substringBeforeLast(guardName, GUARD_NAME_COMPONENT_SEPARATOR);
    	
    	return new DocumentGuardName(new BucketName(guardBucketName),new UserID(userIdName), new DocumentKeyID(documentKeyIDName));
	}

	@Override
	public String toString() {
		return "DocumentGuardName{" +
				"guardBucketName=" + guardBucketName +
				", documentKeyID=" + documentKeyID +
				", userId=" + userId +
				'}';
	}
}
