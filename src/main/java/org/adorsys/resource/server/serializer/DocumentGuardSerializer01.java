package org.adorsys.resource.server.serializer;

import java.io.IOException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.crypto.SecretKey;
import javax.security.auth.callback.CallbackHandler;

import org.adorsys.encobject.service.MissingKeyAlgorithmException;
import org.adorsys.encobject.service.MissingKeystoreAlgorithmException;
import org.adorsys.encobject.service.MissingKeystoreProviderException;
import org.adorsys.encobject.service.WrongKeyCredentialException;
import org.adorsys.encobject.service.WrongKeystoreCredentialException;
import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;
import org.adorsys.resource.server.basetypes.DocumentKey;
import org.adorsys.resource.server.utils.KeystoreAdapter;

public class DocumentGuardSerializer01 implements DocumentGuardSerializer {
	
	/**
	 * This is the proprietary format for the serialization of docu guard. In order to allow for modification or 
	 * migration of serialization format, format-name must be used as file extension.
	 */
	public static final String SERIALIZER_ID = "dgs01";
	private static final String DGS01_KEYID = "keyid";
	private static CallbackHandler keystoreHandler = new PasswordCallbackHandler(null);
	
	/*
	 * Deserializes the secret key. In order not to define a proper key
	 * serialization format, we reuse the keystore format system.
	 */
	public DocumentKey deserializeSecretKey(byte[] decryptedGuardBytes) {
		KeyStore secretKeystore;
		try {
			secretKeystore = KeystoreAdapter.loadKeystoreFromBytes(decryptedGuardBytes, DGS01_KEYID, keystoreHandler);
			SecretKey secretKey = (SecretKey) KeystoreAdapter.readKeyFromKeystore(secretKeystore, DGS01_KEYID, keystoreHandler);
			return new DocumentKey(secretKey);
		} catch (CertificateException | WrongKeystoreCredentialException | MissingKeystoreAlgorithmException
				| MissingKeystoreProviderException | MissingKeyAlgorithmException | IOException | WrongKeyCredentialException e) {
			// Not supposed to happen.
			throw new IllegalStateException(e);
		}
	}

	public byte[] serializeSecretKey(DocumentKey documentKey) {
		try {
			KeyStore docKeyStore = KeystoreAdapter.wrapSecretKey2KeyStore(documentKey.getSecretKey(), DGS01_KEYID,
					keystoreHandler);
			return KeystoreAdapter.toBytes(docKeyStore, DGS01_KEYID, docKeyStore.getType(), keystoreHandler);
		} catch (NoSuchAlgorithmException | CertificateException | IOException e) {
			// Not supposed to happen.
			throw new IllegalStateException(e);
		}
	}
}
