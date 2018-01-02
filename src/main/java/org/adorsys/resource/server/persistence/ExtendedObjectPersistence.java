package org.adorsys.resource.server.persistence;

import java.security.Key;

import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.params.EncParamSelector;
import org.adorsys.encobject.params.EncryptionParams;
import org.adorsys.encobject.service.StoreConnection;
import org.adorsys.jjwk.selector.JWEEncryptedSelector;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.apache.commons.io.IOUtils;

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
	
	/**
	 * Encrypt and stores an byte array given additional meta information and encryption details.
	 * 
	 * @param data : unencrypted version of bytes to store
	 * @param metaInfo : document meta information. e.g. content type, compression, expiration
	 * @param location : location of the document. Includes container name (bucket) and file name.
	 * @param keySource : key producer. Return a key given the keyId
	 * @param keyID : id of the key to be used from source to encrypt the docuement.
	 * @param encParams
	 */
	public void storeObject(byte[] data, ContentMetaInfo metaInfo, ObjectHandle location, KeySource keySource, KeyID keyID,
			EncryptionParams encParams) {
		
		try {
	
			// We accept empty meta info
			if (metaInfo == null)metaInfo=new ContentMetaInfo();
	
			// Retrieve the key.
			Key key = keySource.readKey(keyID);
	
			// Encryption params is optional. If not provided, we select an
			// encryption param based on the key selected.
			if (encParams == null) encParams = EncParamSelector.selectEncryptionParams(key);
	
			Builder headerBuilder = new JWEHeader.Builder(encParams.getEncAlgo(), encParams.getEncMethod()).keyID(keyID.getValue());
			ContentMetaInfoUtils.metaInfo2Header(metaInfo, headerBuilder);
	
			JWEHeader header = headerBuilder.build();
	
			JWEEncrypter jweEncrypter = JWEEncryptedSelector.geEncrypter(key, encParams.getEncAlgo(),
					encParams.getEncMethod());
	
			JWEObject jweObject = new JWEObject(header, new Payload(data));
			jweObject.encrypt(jweEncrypter);
	
			String jweEncryptedObject = jweObject.serialize();
	
			byte[] bytesToStore = jweEncryptedObject.getBytes("UTF-8");

			blobStoreConnection.putBlob(location, bytesToStore);
		} catch (Exception e){
			BaseExceptionHandler.handle(e);
		}
	}

	public PersistentObjectWrapper loadObject(ObjectHandle location, KeySource keySource) {
		
		try {
			
			if (location == null)
				throw new IllegalArgumentException("Object handle must be provided.");
	
			byte[] jweEncryptedBytes = blobStoreConnection.getBlob(location);
			String jweEncryptedObject = IOUtils.toString(jweEncryptedBytes, "UTF-8");

			JWEObject jweObject = JWEObject.parse(jweEncryptedObject);

			KeyID keyID = new KeyID(jweObject.getHeader().getKeyID());
			Key key = keySource.readKey(keyID);

			JWEDecrypter decrypter = decrypterFactory.createJWEDecrypter(jweObject.getHeader(), key);

			jweObject.decrypt(decrypter);

			ContentMetaInfo metaInfo = new ContentMetaInfo();
			ContentMetaInfoUtils.header2MetaInfo(jweObject.getHeader(), metaInfo);
			return new PersistentObjectWrapper(jweObject.getPayload().toBytes(), metaInfo, keyID, location);
		} catch (Exception e) {
			throw BaseExceptionHandler.handle(e);
		}

	}
}
