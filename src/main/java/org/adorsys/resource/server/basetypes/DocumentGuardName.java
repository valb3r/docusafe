package org.adorsys.resource.server.basetypes;

import org.adorsys.resource.server.basetypes.adapter.DocumentGuardNameRestAdapter;

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

	DocumnentKeyID documnentKeyID;
	UserID userId;

	public DocumentGuardName() {}

	
	public DocumentGuardName(String value) {
		super(value);
		fromString(value);
	}

	public DocumentGuardName(UserID userId, DocumnentKeyID documnentKeyID) {
		super(toGuradName(userId, documnentKeyID));
		this.documnentKeyID = documnentKeyID;
		this.userId = userId;
	}

	public DocumnentKeyID getDocumnentKeyID() {
		return documnentKeyID;
	}

	public UserID getUserId() {
		return userId;
	}
	
	private static String toGuradName(UserID userId, DocumnentKeyID documnentKeyID){
		return userId.getValue() + "." + documnentKeyID.getValue();
	}
	
	private static DocumentGuardName fromString(String value){
    	String[] split = value.split(".");
    	return new DocumentGuardName(new UserID(split[0]), new DocumnentKeyID(split[2]));
	}
}
