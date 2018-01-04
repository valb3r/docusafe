package org.adorsys.resource.server.service;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.CallbackHandler;

import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.params.EncryptionParams;
import org.adorsys.encobject.service.KeystorePersistence;
import org.adorsys.encobject.service.ObjectNotFoundException;
import org.adorsys.jjwk.keystore.JwkExport;
import org.adorsys.jjwk.serverkey.KeyAndJwk;
import org.adorsys.jjwk.serverkey.ServerKeyMap;
import org.adorsys.jkeygen.keystore.SecretKeyData;
import org.adorsys.resource.server.basetypes.BucketName;
import org.adorsys.resource.server.basetypes.DocumentGuardName;
import org.adorsys.resource.server.basetypes.DocumentKey;
import org.adorsys.resource.server.basetypes.DocumnentKeyID;
import org.adorsys.resource.server.basetypes.GuardKeyID;
import org.adorsys.resource.server.basetypes.UserID;
import org.adorsys.resource.server.complextypes.DocumentGuard;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.ExtendedObjectPersistence;
import org.adorsys.resource.server.persistence.HexUtil;
import org.adorsys.resource.server.persistence.KeyID;
import org.adorsys.resource.server.persistence.KeySource;
import org.adorsys.resource.server.persistence.KeyStoreBasedKeySourceImpl;
import org.adorsys.resource.server.persistence.PersistentObjectWrapper;
import org.adorsys.resource.server.serializer.DocumentGuardSerializer;
import org.adorsys.resource.server.serializer.DocumentGuardSerializer01;
import org.adorsys.resource.server.serializer.DocumentGuardSerializerRegistery;
import org.adorsys.resource.server.utils.KeyStoreHandleUtils;
import org.apache.commons.lang3.RandomStringUtils;

import com.nimbusds.jose.jwk.JWKSet;

import de.adorsys.resource.server.keyservice.SecretKeyGenerator;

public class DocumentGuardService {
	
	public static final String SERIALIZER_HEADER_KEY="serilizer_id";
	private KeystorePersistence keystorePersistence;
	private SecretKeyGenerator secretKeyGenerator;
	private ExtendedObjectPersistence objectPersistence;
	
	private DocumentGuardSerializerRegistery serializerRegistry = DocumentGuardSerializerRegistery.getInstance();

	public DocumentGuardService(KeystorePersistence keystorePersistence, ExtendedObjectPersistence objectPersistence) {
		this.keystorePersistence = keystorePersistence;
		this.objectPersistence = objectPersistence;
		this.secretKeyGenerator = new SecretKeyGenerator("AES", 256);
	}

	/**
	 * 
	 * @param userId
	 * @param userKeystoreHandler
	 * @param keyPassHandler
	 */
	public DocumentGuardName createUserSelfGuard(UserID userId, CallbackHandler userKeystoreHandler, CallbackHandler keyPassHandler,
			BucketName keystoreBucketName, BucketName guardBucketName)  {
		try {
			ObjectHandle keystoreHandle = KeyStoreHandleUtils.userkeyStoreHandle(keystoreBucketName, userId);
			KeyStore userKeystore = keystorePersistence.loadKeystore(keystoreHandle, userKeystoreHandler);
			
			JWKSet jwkSet = JwkExport.exportKeys(userKeystore, userKeystoreHandler);
			ServerKeyMap serverKeyMap = new ServerKeyMap(jwkSet);
			KeyAndJwk randomSecretKey = serverKeyMap.randomSecretKey();
			GuardKeyID guardKeyID = new GuardKeyID(randomSecretKey.jwk.getKeyID());
			
			// Generate a secret key
			String keyAlias = RandomStringUtils.randomAlphanumeric(20);
			SecretKeyData secretKeyData = secretKeyGenerator.generate(keyAlias,keyPassHandler);
			byte[] serializeSecretKeyBytes = serializerRegistry.defaultSerializer().serializeSecretKey(new DocumentKey(secretKeyData.getSecretKey()));

			System.out.println("SAVE DOCUMENTKEY:" + HexUtil.conventBytesToHexString(serializeSecretKeyBytes));
			
			DocumnentKeyID documnentKeyID = new DocumnentKeyID(secretKeyData.getAlias());
			DocumentGuardName documentGuardName = new DocumentGuardName(guardBucketName, userId, documnentKeyID);
			
			ObjectHandle location = new ObjectHandle(guardBucketName.getValue(), documentGuardName.getValue());
			EncryptionParams encParams = null;
			ContentMetaInfo metaInfo = new ContentMetaInfo();
			metaInfo.setAddInfos(new HashMap<>());
			metaInfo.getAddInfos().put(SERIALIZER_HEADER_KEY, DocumentGuardSerializer01.SERIALIZER_ID);
			KeySource keySource = new KeyStoreBasedKeySourceImpl(userKeystore, keyPassHandler);
			objectPersistence.storeObject(serializeSecretKeyBytes, metaInfo, location, keySource, new KeyID(guardKeyID.getValue()), encParams);
			
			return documentGuardName;
		} catch (Exception e) {
			throw BaseExceptionHandler.handle(e);
		}
	}

    /**
     * Loading the secret key from the guard.
     */
    public DocumentGuard loadDocumentGuard(DocumentGuardName documentGuardName, BucketName keystoreBucketName, CallbackHandler userKeystoreHandler,
                                             CallbackHandler userKeyPassHandler){
    	
    	try {
	
	        ObjectHandle keystoreHandle = KeyStoreHandleUtils.userkeyStoreHandle(keystoreBucketName, documentGuardName.getUserId());
	        if (!keystorePersistence.hasKeystore(keystoreHandle)) {
	            throw new ObjectNotFoundException("user keystore not found.");
	        }
	        KeyStore userKeystore = keystorePersistence.loadKeystore(keystoreHandle, userKeystoreHandler);
	
	
	        // load guard file
	        ObjectHandle guardHandle = new ObjectHandle(documentGuardName.getGuardBucketName().getValue(), documentGuardName.getValue());
	        KeySource keySource = new KeyStoreBasedKeySourceImpl(userKeystore, userKeyPassHandler);
	        PersistentObjectWrapper wrapper = objectPersistence.loadObject(guardHandle, keySource);
	        
	        ContentMetaInfo metaIno = wrapper.getMetaIno();
	        Map<String, Object> addInfos = metaIno.getAddInfos();
	        String serializerId = (String) addInfos.get(SERIALIZER_HEADER_KEY);
			serializerRegistry.getSerializer(serializerId);
			DocumentGuardSerializer serializer = serializerRegistry.getSerializer(serializerId);
	        DocumentKey documentKey = serializer.deserializeSecretKey(wrapper.getData());
	        return new DocumentGuard(documentGuardName, documentKey);
    	} catch(Exception e){
    		throw BaseExceptionHandler.handle(e);
    	}
    }
}
