package org.adorsys.docusafe.service.serializer;

import org.adorsys.docusafe.service.types.DocumentKey;
import org.adorsys.jkeygen.keystore.KeyStoreType;

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
