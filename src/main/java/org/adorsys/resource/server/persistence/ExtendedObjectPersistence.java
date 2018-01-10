package org.adorsys.resource.server.persistence;

import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEHeader.Builder;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.factories.DefaultJWEDecrypterFactory;
import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.params.EncryptionParams;
import org.adorsys.encobject.service.StoreConnection;
import org.adorsys.jjwk.selector.JWEEncryptedSelector;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.exceptions.ExtendedPersistenceException;
import org.adorsys.resource.server.persistence.basetypes.KeyID;
import org.adorsys.resource.server.persistence.keysource.KeySource;
import org.apache.commons.io.IOUtils;

import java.security.Key;

/**
 * This class jwe encrypt and store bytes, loads and jwe decrypt bytes. For this purpose, this class always add a ".jwe" to the file name. 
 * It will stripp this".jwe" file extension before returning the byte to the caller. 
 * 
 * e.g.:
 * -> if you store "peter.doc" in the storage you will find "peter.doc.jwe"
 * -> if you request "peter.doc", we will look for "peter.doc.jwe"
 * 
 * @author fpo
 *
 */
public class ExtendedObjectPersistence {

	private static final String EXTENSION = ".jwe";

	private DefaultJWEDecrypterFactory decrypterFactory = new DefaultJWEDecrypterFactory();

	private final StoreConnection blobStoreConnection;

	public ExtendedObjectPersistence(StoreConnection blobStoreConnection) {
		this.blobStoreConnection = blobStoreConnection;
	}
	
	/**
	 * Encrypt and stores an byte array given additional meta information and encryption details.
	 * 
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
			if (encParams == null) encParams = ExtendedEncParamSelector.selectEncryptionParams(key);

			Builder headerBuilder = new JWEHeader.Builder(encParams.getEncAlgo(), encParams.getEncMethod()).keyID(keyID.getValue());
			ContentMetaInfoUtils.metaInfo2Header(metaInfo, headerBuilder);
	
			JWEHeader header = headerBuilder.build();
	
			JWEEncrypter jweEncrypter = JWEEncryptedSelector.geEncrypter(key, encParams.getEncAlgo(),
					encParams.getEncMethod());
	
			JWEObject jweObject = new JWEObject(header, new Payload(data));
			jweObject.encrypt(jweEncrypter);
	
			String jweEncryptedObject = jweObject.serialize();
	
			byte[] bytesToStore = jweEncryptedObject.getBytes("UTF-8");

			blobStoreConnection.putBlob(addExtension(location), bytesToStore);
		} catch (Exception e){
			BaseExceptionHandler.handle(e);
		}
	}

	private ObjectHandle addExtension(ObjectHandle location) {
		return new ObjectHandle(location.getContainer(), location.getName()+ EXTENSION);
	}

	public PersistentObjectWrapper loadObject(ObjectHandle location, KeySource keySource) {
		
		try {
			if (location == null)
				throw new ExtendedPersistenceException("Location for Object must not be null.");
			
			location = addExtension(location);
	
			byte[] jweEncryptedBytes = blobStoreConnection.getBlob(location);
			String jweEncryptedObject = IOUtils.toString(jweEncryptedBytes, "UTF-8");

			JWEObject jweObject = JWEObject.parse(jweEncryptedObject);

			KeyID keyID = new KeyID(jweObject.getHeader().getKeyID());
			Key key = keySource.readKey(keyID);

			if (key == null) {
				throw new ExtendedPersistenceException("can not read key with keyID " + keyID + " from keySource of class " + keySource.getClass().getName());
			}

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
