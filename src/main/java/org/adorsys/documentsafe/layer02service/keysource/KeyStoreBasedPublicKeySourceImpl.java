package org.adorsys.documentsafe.layer02service.keysource;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.AssymetricJWK;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import org.adorsys.documentsafe.layer00common.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer02service.exceptions.KeySourceException;
import org.adorsys.documentsafe.layer01persistence.types.KeyID;
import org.adorsys.documentsafe.layer01persistence.keysource.KeySource;
import org.adorsys.jjwk.keystore.JwkExport;

import java.security.Key;

public class KeyStoreBasedPublicKeySourceImpl implements KeySource {


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
			throw new KeySourceException("key with id " +keyID.getValue()  + " not instance of AssymetricJWK");
		}
	}
}
