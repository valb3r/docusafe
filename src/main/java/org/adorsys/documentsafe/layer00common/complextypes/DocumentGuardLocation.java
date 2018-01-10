package org.adorsys.documentsafe.layer00common.complextypes;

import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.documentsafe.layer00common.basetypes.DocumentKeyID;

/**
 * Created by peter on 23.12.2017 at 18:33:14.
 * 
 */
public abstract class DocumentGuardLocation {
	public static final String GUARD_NAME_COMPONENT_SEPARATOR = ".";
	public static ObjectHandle getLocationHandle(KeyStoreLocation keyStoreLocation, DocumentKeyID documentKeyID){
		return new ObjectHandle(
				keyStoreLocation.getLocationHandle().getContainer(),
				keyStoreLocation.getLocationHandle().getName() + GUARD_NAME_COMPONENT_SEPARATOR + documentKeyID.getValue());
	}
}