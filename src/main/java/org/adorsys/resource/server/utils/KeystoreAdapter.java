package org.adorsys.resource.server.utils;

import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.SecretKey;
import javax.security.auth.callback.CallbackHandler;

import org.adorsys.encobject.domain.keystore.KeystoreData;
import org.adorsys.encobject.service.MissingKeyAlgorithmException;
import org.adorsys.encobject.service.MissingKeystoreAlgorithmException;
import org.adorsys.encobject.service.MissingKeystoreProviderException;
import org.adorsys.encobject.service.WrongKeyCredentialException;
import org.adorsys.encobject.service.WrongKeystoreCredentialException;
import org.adorsys.jkeygen.keystore.KeyStoreService;
import org.adorsys.jkeygen.keystore.KeystoreBuilder;
import org.adorsys.jkeygen.keystore.PasswordCallbackUtils;
import org.adorsys.jkeygen.keystore.SecretKeyData;
import org.adorsys.resource.server.basetypes.DocumnentKeyID;

/**
 * Keystore utilities.
 * 
 * @author fpo
 *
 */
public class KeystoreAdapter {

	/**
	 * Wrap a SecretKey into a keystore.
	 * 
	 * @param secretKey
	 * @param alias
	 * @param keyPassHandler
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 */
	public static KeyStore wrapSecretKEy2KeyStore(SecretKey secretKey, DocumnentKeyID documnentKeyID, CallbackHandler keyPassHandler) throws NoSuchAlgorithmException, CertificateException, IOException {
		SecretKeyData secretKeyData = new SecretKeyData(secretKey, documnentKeyID.getValue(), keyPassHandler);
		return new KeystoreBuilder().withKeyEntry(secretKeyData).build();
	}
	
	/**
	 * Load keystore from bytes, using the format defined in /encobject/src/main/protobuf/KeystorePbf.proto
	 * 
	 * @param keyStoreBytes
	 * @param storeid
	 * @param keystoreHandler
	 * @return
	 * @throws CertificateException
	 * @throws WrongKeystoreCredentialException
	 * @throws MissingKeystoreAlgorithmException
	 * @throws MissingKeystoreProviderException
	 * @throws MissingKeyAlgorithmException
	 * @throws IOException
	 */
	public static KeyStore fromBytes(byte[] keyStoreBytes, String storeid, CallbackHandler keystoreHandler) throws CertificateException, WrongKeystoreCredentialException, MissingKeystoreAlgorithmException, MissingKeystoreProviderException, MissingKeyAlgorithmException, IOException{
		KeystoreData keystoreData = loadKeystoreData(keyStoreBytes);
		return initKeystore(keystoreData, storeid, keystoreHandler);
	}
	
	/**
	 * Retrieves the key with the given keyID from the keystore. The key password will be retrieved by
	 * calling the keyPassHandler passing the keyId.
	 */
	public static Key readKey(KeyStore keyStore, String keyID, CallbackHandler keyPassHandler) throws WrongKeyCredentialException {
		try {
			return keyStore.getKey(keyID, PasswordCallbackUtils.getPassword(keyPassHandler, keyID));
		} catch (UnrecoverableKeyException e) {
			throw new WrongKeyCredentialException(e);
		} catch (KeyStoreException e) {
			throw new WrongKeyCredentialException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}		
	}

	private static KeystoreData loadKeystoreData(byte[] keyStoreBytes)  {
		try {
			return KeystoreData.parseFrom(keyStoreBytes);
		} catch (IOException e) {
			throw new IllegalStateException("Invalid protocol buffer", e);
		}
	}

	private static KeyStore initKeystore(KeystoreData keystoreData, String storeid, CallbackHandler handler) throws WrongKeystoreCredentialException, MissingKeystoreAlgorithmException, MissingKeystoreProviderException, MissingKeyAlgorithmException, CertificateException, IOException {
		try {
			return KeyStoreService.loadKeyStore(keystoreData.getKeystore().toByteArray(), storeid, keystoreData.getType(), handler);
		} catch (UnrecoverableKeyException e) {
			throw new WrongKeystoreCredentialException(e);
		} catch (KeyStoreException e) {
			if(e.getCause()!=null){
				Throwable cause = e.getCause();
				if(cause instanceof NoSuchAlgorithmException){
					throw new MissingKeystoreAlgorithmException(cause.getMessage(), cause);
				}
				if(cause instanceof NoSuchProviderException){
					throw new MissingKeystoreProviderException(cause.getMessage(), cause);
				}
			}
			throw new IllegalStateException("Unidentified keystore exception", e);
		} catch (NoSuchAlgorithmException e) {
			throw new MissingKeyAlgorithmException(e.getMessage(), e);
		}
	}	
}
