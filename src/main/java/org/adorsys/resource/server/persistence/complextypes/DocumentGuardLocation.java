package org.adorsys.resource.server.persistence.complextypes;

import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.resource.server.basetypes.DocumentKeyID;

/**
 * Created by peter on 23.12.2017 at 18:33:14.
 * 
 */
public class DocumentGuardLocation {
	private static final long serialVersionUID = -3042461057378981231L;

	public static final String GUARD_NAME_COMPONENT_SEPARATOR = ".";
	
	private DocumentKeyID documentKeyID;
	private KeyStoreLocation keyStoreLocation;

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
		return new ObjectHandle(
				keyStoreLocation.getKeyStoreBucketName().getValue(),
				keyStoreLocation.toFileName() + GUARD_NAME_COMPONENT_SEPARATOR + documentKeyID.getValue());
	}

	@Override
	public String toString() {
		return "DocumentGuardLocation{" +
				"documentKeyID=" + documentKeyID +
				", keyStoreLocation=" + keyStoreLocation +
				'}';
	}
}
