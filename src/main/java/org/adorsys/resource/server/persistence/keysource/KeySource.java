package org.adorsys.resource.server.persistence.keysource;

import java.security.Key;

import org.adorsys.resource.server.persistence.basetypes.KeyID;

/**
 * Retrieves and returns the key with the corresponding keyId.
 * 
 * @author fpo
 *
 */
public interface KeySource {
	public Key readKey(KeyID keyID);
}
