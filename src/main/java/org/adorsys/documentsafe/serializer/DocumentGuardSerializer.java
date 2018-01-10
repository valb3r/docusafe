package org.adorsys.documentsafe.serializer;

import org.adorsys.documentsafe.persistence.basetypes.DocumentKey;

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
