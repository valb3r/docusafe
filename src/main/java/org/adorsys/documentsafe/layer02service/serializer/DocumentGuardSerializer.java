package org.adorsys.documentsafe.layer02service.serializer;

import org.adorsys.documentsafe.layer02service.types.DocumentKey;

/**
 * Serializer for the content of a document guard.  
 * 
 * @author fpo
 *
 */
public interface DocumentGuardSerializer {

	public DocumentKey deserializeSecretKey(byte[] decryptedGuardBytes);

	public byte[] serializeSecretKey(DocumentKey documentKey);
}
