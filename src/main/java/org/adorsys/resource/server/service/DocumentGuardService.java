package org.adorsys.resource.server.service;

import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.CallbackHandler;

import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.params.EncryptionParams;
import org.adorsys.encobject.service.KeystoreNotFoundException;
import org.adorsys.encobject.service.KeystorePersistence;
import org.adorsys.encobject.service.MissingKeyAlgorithmException;
import org.adorsys.encobject.service.MissingKeystoreAlgorithmException;
import org.adorsys.encobject.service.MissingKeystoreProviderException;
import org.adorsys.encobject.service.ObjectNotFoundException;
import org.adorsys.encobject.service.UnknownContainerException;
import org.adorsys.encobject.service.WrongKeyCredentialException;
import org.adorsys.encobject.service.WrongKeystoreCredentialException;
import org.adorsys.jjwk.keystore.JwkExport;
import org.adorsys.jjwk.selector.UnsupportedEncAlgorithmException;
import org.adorsys.jjwk.selector.UnsupportedKeyLengthException;
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
import org.adorsys.resource.server.persistence.ExtendedObjectPersistence;
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
	DocumentGuardSerializer serializer = new DocumentGuardSerializer01();
	private KeystorePersistence keystorePersistence;
	private SecretKeyGenerator secretKeyGenerator;
	private ExtendedObjectPersistence objectPersistence;

	public DocumentGuardService(KeystorePersistence keystorePersistence, ExtendedObjectPersistence objectPersistence) {
		this.keystorePersistence = keystorePersistence;
		this.objectPersistence = objectPersistence;
		secretKeyGenerator = new SecretKeyGenerator("AES", 256);
	}

	public void createUserSelfGuard(UserID userId, CallbackHandler userKeystoreHandler, CallbackHandler keyPassHandler,
			BucketName bucketName) throws KeystoreNotFoundException, CertificateException,
			WrongKeystoreCredentialException, MissingKeystoreAlgorithmException, MissingKeystoreProviderException,
			MissingKeyAlgorithmException, IOException, UnknownContainerException, UnsupportedEncAlgorithmException, WrongKeyCredentialException, UnsupportedKeyLengthException {
		ObjectHandle keystoreHandle = KeyStoreHandleUtils.userkeyStoreHandle(bucketName, userId);
		KeyStore userKeystore = keystorePersistence.loadKeystore(keystoreHandle, userKeystoreHandler);
		
		JWKSet jwkSet = JwkExport.exportKeys(userKeystore, userKeystoreHandler);
		ServerKeyMap serverKeyMap = new ServerKeyMap(jwkSet);
		KeyAndJwk randomSecretKey = serverKeyMap.randomSecretKey();
		GuardKeyID guardKeyID = new GuardKeyID(randomSecretKey.jwk.getKeyID());
		
		SecretKeyData secretKeyData = secretKeyGenerator.generate(userId.getValue() + RandomStringUtils.randomAlphanumeric(5).toUpperCase(),keyPassHandler);
		byte[] serializeSecretKeyBytes = serializer.serializeSecretKey(new DocumentKey(secretKeyData.getSecretKey()));
		
		
		DocumnentKeyID documnentKeyID = new DocumnentKeyID(secretKeyData.getAlias());
		DocumentGuardName documentGuardName = new DocumentGuardName(userId, guardKeyID, documnentKeyID);
		
		ObjectHandle handle = new ObjectHandle(bucketName.getValue(), documentGuardName.getValue());
		EncryptionParams encParams = null;
		ContentMetaInfo metaIno = new ContentMetaInfo();
		metaIno.setAddInfos(new HashMap<>());
		metaIno.getAddInfos().put(SERIALIZER_HEADER_KEY, DocumentGuardSerializer01.SERIALIZER_ID);
		PersistentObjectWrapper wrapper = new PersistentObjectWrapper(serializeSecretKeyBytes, metaIno, guardKeyID.getValue(), handle);
		objectPersistence.storeObject(wrapper, userKeystore, keyPassHandler, encParams);
	}

    /*
     * Loading the secret key from the guard.
     */
    public DocumentGuard loadDocumentGuard(UserID userID, BucketName bucketName, DocumentGuardName documentGuardName, CallbackHandler userKeystoreHandler,
                                             CallbackHandler userKeyPassHandler) throws ObjectNotFoundException, CertificateException, WrongKeystoreCredentialException, MissingKeystoreAlgorithmException, MissingKeystoreProviderException, MissingKeyAlgorithmException, IOException, UnknownContainerException, WrongKeyCredentialException {

        ObjectHandle keystoreHandle = KeyStoreHandleUtils.userkeyStoreHandle(bucketName, userID);
        if (!keystorePersistence.hasKeystore(keystoreHandle)) {
            throw new ObjectNotFoundException("user keystore not found.");
        }
        KeyStore userKeystore = keystorePersistence.loadKeystore(keystoreHandle, userKeystoreHandler);


        // load guard file
        ObjectHandle guardHandle = new ObjectHandle(bucketName.getValue(), documentGuardName.getValue());
        PersistentObjectWrapper wrapper = new PersistentObjectWrapper(null, null, null, guardHandle);
        objectPersistence.loadObject(wrapper, userKeystore, userKeyPassHandler);

        ContentMetaInfo metaIno = wrapper.getMetaIno();
        Map<String, Object> addInfos = metaIno.getAddInfos();
        Object serializerId = addInfos.get(SERIALIZER_HEADER_KEY);
        if(serializerId==null) throw new IllegalStateException("Missing meta info serializer");
        DocumentGuardSerializer serializer = DocumentGuardSerializerRegistery.getInstance().getSerializer(serializerId.toString());
        DocumentKey documentKey = serializer.deserializeSecretKey(wrapper.getData());
        return new DocumentGuard(documentGuardName, documentKey);
    }

}
