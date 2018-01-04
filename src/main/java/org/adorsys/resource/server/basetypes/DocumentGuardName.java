package org.adorsys.resource.server.basetypes;

import org.adorsys.resource.server.basetypes.adapter.DocumentGuardNameRestAdapter;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by peter on 23.12.2017 at 18:33:14.
 * 
 * @TODO: Add following fields:
 * - UserID
 * - GuardKeyID
 * - DocumentKeyID
 */
@XmlJavaTypeAdapter(DocumentGuardNameRestAdapter.class)
@XmlType
public class DocumentGuardName extends BaseTypeString {
	private static final long serialVersionUID = -3042461057378981231L;

	/**
	 * Separator between guard name and containing bucket.
	 */
	public static final String BUCKET_SEPARATOR = "@";
	
	public static final String GUARD_NAME_COMPONENT_SEPARATOR = ".";
	
	private BucketName guardBucketName;
	private DocumnentKeyID documnentKeyID;
	private UserID userId;

	public DocumentGuardName() {}

	
	public DocumentGuardName(String value) {
		super(value);
		fromString(value);
	}

	public DocumentGuardName(BucketName guardBucketName, UserID userId, DocumnentKeyID documnentKeyID) {
		super(toGuradName(guardBucketName, userId, documnentKeyID));
		this.documnentKeyID = documnentKeyID;
		this.userId = userId;
		this.guardBucketName = guardBucketName;
	}

	public DocumnentKeyID getDocumnentKeyID() {
		return documnentKeyID;
	}

	public UserID getUserId() {
		return userId;
	}
	
	public BucketName getGuardBucketName() {
		return guardBucketName;
	}


	private static String toGuradName(BucketName guardBucketName, UserID userId, DocumnentKeyID documnentKeyID){
		return userId.getValue() + GUARD_NAME_COMPONENT_SEPARATOR + documnentKeyID.getValue() + BUCKET_SEPARATOR + guardBucketName.getValue();
	}
	
	private static DocumentGuardName fromString(String guardFQN){
		String guardBucketName = StringUtils.substringAfterLast(guardFQN, BUCKET_SEPARATOR);
		String guardName = StringUtils.substringBeforeLast(guardFQN, BUCKET_SEPARATOR);
		String documnentKeyIDName = StringUtils.substringAfterLast(guardName, GUARD_NAME_COMPONENT_SEPARATOR);
		String userIdName = StringUtils.substringBeforeLast(guardName, GUARD_NAME_COMPONENT_SEPARATOR);
    	
    	return new DocumentGuardName(new BucketName(guardBucketName),new UserID(userIdName), new DocumnentKeyID(documnentKeyIDName));
	}

}
