package org.adorsys.documentsafe.layer02service.types.complextypes;

import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.complextypes.KeyStoreLocation;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;

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
	public static BucketPath getBucketPathOfGuard(KeyStoreLocation keyStoreLocation, DocumentKeyID documentKeyID){
        ObjectHandle locationHandle = getLocationHandle(keyStoreLocation, documentKeyID);
        return new BucketPath(locationHandle.getContainer(), locationHandle.getName());
	}
}
