package org.adorsys.documentsafe.layer02service.types.complextypes;

import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.encobject.complextypes.BucketPath;

/**
 * Created by peter on 23.12.2017 at 18:33:14.
 * 
 */
public abstract class DocumentGuardLocation {
	public static final String GUARD_NAME_COMPONENT_SEPARATOR = ".";
	public static BucketPath getBucketPathOfGuard(BucketPath keyStorePath, DocumentKeyID documentKeyID){
        return keyStorePath.add(GUARD_NAME_COMPONENT_SEPARATOR + documentKeyID.getValue());
	}
}
