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
	DocumnentKeyID documnentKeyID;
	GuardKeyID guardKeyID;
	UserID userId;

	public DocumentGuardName() {}

	
	public DocumentGuardName(String value) {
		super(value);
		fromString(value);
	}

	public DocumentGuardName(UserID userId, GuardKeyID guardKeyID, DocumnentKeyID documnentKeyID) {
		super(toGuradName(userId, guardKeyID, documnentKeyID));
		this.documnentKeyID = documnentKeyID;
		this.guardKeyID = guardKeyID;
		this.userId = userId;
	}

	public DocumnentKeyID getDocumnentKeyID() {
		return documnentKeyID;
	}

	public GuardKeyID getGuardKeyID() {
		return guardKeyID;
	}

	public UserID getUserId() {
		return userId;
	}
	
	private static String toGuradName(UserID userId, GuardKeyID guardKeyID, DocumnentKeyID documnentKeyID){
		return userId.getValue() + "." + guardKeyID.getValue() + "." + documnentKeyID.getValue();
	}
	
	private static DocumentGuardName fromString(String value){
    	String[] split = value.split(".");
    	return new DocumentGuardName(new UserID(split[0]), new GuardKeyID(split[1]), new DocumnentKeyID(split[2]));
	}
}
