package org.adorsys.documentsafe.layer02service.serializer;

import org.adorsys.documentsafe.layer02service.types.DocumentKey;
import org.adorsys.encobject.types.KeyStoreType;

/**
 * Serializer for the content of a document guard.  
 * 
 * @author fpo
 *
 */
public interface DocumentGuardSerializer {

	public DocumentKey deserializeSecretKey(byte[] decryptedGuardBytes, KeyStoreType keyStoreType);

	public byte[] serializeSecretKey(DocumentKey documentKey, KeyStoreType keyStoreType);

	public String getSerializerID();
}
