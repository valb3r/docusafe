package org.adorsys.resource.server.serializer;

import org.adorsys.resource.server.basetypes.DocumentKey;

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
