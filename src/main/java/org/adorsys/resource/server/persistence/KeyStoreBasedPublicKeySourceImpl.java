package org.adorsys.resource.server.persistence;

import java.security.Key;

import org.adorsys.jjwk.keystore.JwkExport;
import org.adorsys.resource.server.exceptions.BaseException;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.basetypes.KeyID;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.AssymetricJWK;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;

public class KeyStoreBasedPublicKeySourceImpl implements KeySource{


	private JWKSet keys;


	public KeyStoreBasedPublicKeySourceImpl(JWKSet keys) {
		this.keys = keys;
	}


	@Override
	public Key readKey(KeyID keyID) {
		JWK jwk = JwkExport.selectKey(keys, keyID.getValue());
		if (jwk instanceof AssymetricJWK) {
			try {
				return ((AssymetricJWK) jwk).toPublicKey();
			} catch (JOSEException e) {
				throw BaseExceptionHandler.handle(e);
			}
		} else {
			throw new BaseException("key with id " +keyID.getValue()  + " not instance of AssymetricJWK");
		}
	}
}
