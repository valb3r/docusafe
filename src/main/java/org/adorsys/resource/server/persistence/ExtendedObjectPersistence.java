package org.adorsys.resource.server.persistence;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.text.ParseException;

import javax.security.auth.callback.CallbackHandler;

import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.params.EncParamSelector;
import org.adorsys.encobject.params.EncryptionParams;
import org.adorsys.encobject.service.ObjectNotFoundException;
import org.adorsys.encobject.service.StoreConnection;
import org.adorsys.encobject.service.UnknownContainerException;
import org.adorsys.encobject.service.WrongKeyCredentialException;
import org.adorsys.jjwk.selector.JWEEncryptedSelector;
import org.adorsys.jjwk.selector.UnsupportedEncAlgorithmException;
import org.adorsys.jjwk.selector.UnsupportedKeyLengthException;
import org.adorsys.jkeygen.keystore.PasswordCallbackUtils;
import org.apache.commons.io.IOUtils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEHeader.Builder;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.factories.DefaultJWEDecrypterFactory;

public class ExtendedObjectPersistence {

	private DefaultJWEDecrypterFactory decrypterFactory = new DefaultJWEDecrypterFactory();

	private final StoreConnection blobStoreConnection;

	public ExtendedObjectPersistence(StoreConnection blobStoreConnection) {
		this.blobStoreConnection = blobStoreConnection;
	}

	public void storeObject(PersistentObjectWrapper wrapper, KeyStore keyStore, CallbackHandler keyPassHandler,
			EncryptionParams encParams) throws UnsupportedEncAlgorithmException, WrongKeyCredentialException,
			UnsupportedKeyLengthException, UnknownContainerException {

		// We accept empty meta info
		if (wrapper.getMetaIno() == null)wrapper.setMetaIno(new ContentMetaInfo());
		ContentMetaInfo metaIno = wrapper.getMetaIno();
		String keyID = wrapper.getKeyID();

		// Retrieve the key.
		Key key = readKey(keyStore, keyID, keyPassHandler);

		// Encryption params is optional. If not provided, we select an
		// encryption param based on the key selected.
		if (encParams == null) encParams = EncParamSelector.selectEncryptionParams(key);

		Builder headerBuilder = new JWEHeader.Builder(encParams.getEncAlgo(), encParams.getEncMethod()).keyID(keyID);
		ContentMetaInfoUtils.metaInfo2Header(metaIno, headerBuilder);

		JWEHeader header = headerBuilder.build();

		JWEEncrypter jweEncrypter = JWEEncryptedSelector.geEncrypter(key, encParams.getEncAlgo(),
				encParams.getEncMethod());

		JWEObject jweObject = new JWEObject(header, new Payload(wrapper.getData()));

		try {
			jweObject.encrypt(jweEncrypter);
		} catch (JOSEException e) {
			throw new IllegalStateException("Encryption error", e);
		}

		String jweEncryptedObject = jweObject.serialize();

		byte[] bytesToStore;
		try {
			bytesToStore = jweEncryptedObject.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Unsupported content type", e);
		}
		blobStoreConnection.putBlob(wrapper.getHandle(), bytesToStore);
	}

	public void loadObject(final PersistentObjectWrapper wrapper, KeyStore keyStore, CallbackHandler keyPassHandler)
			throws ObjectNotFoundException, WrongKeyCredentialException, UnknownContainerException {
		if (wrapper.getHandle() == null)
			throw new IllegalArgumentException("Object handle must be provided.");

		byte[] jweEncryptedBytes = blobStoreConnection.getBlob(wrapper.getHandle());
		String jweEncryptedObject;
		try {
			jweEncryptedObject = IOUtils.toString(jweEncryptedBytes, "UTF-8");
		} catch (IOException e) {
			throw new IllegalStateException("Unsupported content type", e);
		}
		JWEObject jweObject;
		try {
			jweObject = JWEObject.parse(jweEncryptedObject);
		} catch (ParseException e) {
			throw new IllegalStateException("Can not parse jwe object", e);
		}
		String keyID = jweObject.getHeader().getKeyID();
		Key key = readKey(keyStore, keyID, keyPassHandler);

		JWEDecrypter decrypter;
		try {
			decrypter = decrypterFactory.createJWEDecrypter(jweObject.getHeader(), key);
		} catch (JOSEException e) {
			throw new IllegalStateException("No suitable key found", e);
		}
		try {
			jweObject.decrypt(decrypter);
		} catch (JOSEException e) {
			throw new WrongKeyCredentialException(e);
		}
		wrapper.setKeyID(keyID);
		wrapper.setData(jweObject.getPayload().toBytes());
		ContentMetaInfo metaIno = new ContentMetaInfo();
		wrapper.setMetaIno(metaIno);
		ContentMetaInfoUtils.header2MetaInfo(jweObject.getHeader(), metaIno);
	}

	/*
	 * Retrieves the key with the given keyID from the keystore. The key
	 * password will be retrieved by calling the keyPassHandler.
	 */
	private Key readKey(KeyStore keyStore, String keyID, CallbackHandler keyPassHandler)
			throws WrongKeyCredentialException {
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
}
