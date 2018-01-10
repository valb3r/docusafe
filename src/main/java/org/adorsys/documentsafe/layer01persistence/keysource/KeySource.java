package org.adorsys.documentsafe.layer01persistence.keysource;

import java.security.Key;

import org.adorsys.documentsafe.layer00common.basetypes.KeyID;

/**
 * Retrieves and returns the key with the corresponding keyId.
 * 
 * @author fpo
 *
 */
public interface KeySource {
	public Key readKey(KeyID keyID);
}