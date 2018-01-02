package org.adorsys.resource.server.persistence;

import java.security.Key;

/**
 * Retrieves and returns the key with the corresponding keyId.
 * 
 * @author fpo
 *
 */
public interface KeySource {
	public Key readKey(KeyID keyID);
}
