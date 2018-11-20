package org.adorsys.docusafe.service;

import org.adorsys.encobject.domain.KeyStoreAccess;
import org.adorsys.encobject.service.api.KeySource;

import com.nimbusds.jose.jwk.JWK;
import org.adorsys.encobject.types.PublicKeyJWK;

/**
 * 
 * @author fpo 2018-03-17 10:07
 *
 */
public interface KeySourceService {
	/**
	 * Returns an encryption public key associated with the given keystore access.
	 * 
	 * Used by the sender to encrypt a message. Receiver key passowrd must not be available.
	 * 
	 * @param keyStoreAccess : the keystore access of the receiver of a message.
	 * @return
	 */
	PublicKeyJWK findPublicEncryptionKey(KeyStoreAccess keyStoreAccess);
	
	/**
	 * Returns the keysource of the receiver of a message. User by the receiver
	 * to decrypt the message. Receiver key password must be available.
	 * 
	 * @param keyStoreAccess
	 * @return
	 */
	KeySource getPrivateKeySource(KeyStoreAccess keyStoreAccess);
}
