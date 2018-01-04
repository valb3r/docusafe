package org.adorsys.resource.server.persistence;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.security.auth.callback.CallbackHandler;

import org.adorsys.jkeygen.keystore.PasswordCallbackUtils;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.basetypes.KeyID;

public class KeyStoreBasedKeySourceImpl implements KeySource{

	private KeyStore keyStore;
	private CallbackHandler keyPassHandler;
	
	public KeyStoreBasedKeySourceImpl(KeyStore keyStore, CallbackHandler keyPassHandler) {
		super();
		this.keyStore = keyStore;
		this.keyPassHandler = keyPassHandler;
	}


	@Override
	public Key readKey(KeyID keyID) {
		return readKey(keyStore, keyID, keyPassHandler);
	}


	/*
	 * Retrieves the key with the given keyID from the keystore. The key
	 * password will be retrieved by calling the keyPassHandler.
	 */
	private Key readKey(KeyStore keyStore, KeyID keyID, CallbackHandler keyPassHandler){
		try {
			return keyStore.getKey(keyID.getValue(), PasswordCallbackUtils.getPassword(keyPassHandler, keyID.getValue()));
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
			throw BaseExceptionHandler.handle(e);
		}

	}
}
