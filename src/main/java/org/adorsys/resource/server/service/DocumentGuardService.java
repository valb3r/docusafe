package org.adorsys.resource.server.service;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.CallbackHandler;

import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.params.EncryptionParams;
import org.adorsys.jjwk.keystore.JwkExport;
import org.adorsys.jjwk.serverkey.KeyAndJwk;
import org.adorsys.jjwk.serverkey.ServerKeyMap;
import org.adorsys.jkeygen.keystore.SecretKeyData;
import org.adorsys.resource.server.basetypes.DocumentGuardName;
import org.adorsys.resource.server.basetypes.DocumentKey;
import org.adorsys.resource.server.basetypes.DocumentKeyID;
import org.adorsys.resource.server.basetypes.GuardKeyID;
import org.adorsys.resource.server.basetypes.UserID;
import org.adorsys.resource.server.complextypes.DocumentGuard;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.ExtendedKeystorePersistence;
import org.adorsys.resource.server.persistence.ExtendedObjectPersistence;
import org.adorsys.resource.server.persistence.KeySource;
import org.adorsys.resource.server.persistence.KeyStoreBasedKeySourceImpl;
import org.adorsys.resource.server.persistence.PersistentObjectWrapper;
import org.adorsys.resource.server.persistence.basetypes.BucketName;
import org.adorsys.resource.server.persistence.basetypes.KeyID;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreName;
import org.adorsys.resource.server.serializer.DocumentGuardSerializer;
import org.adorsys.resource.server.serializer.DocumentGuardSerializer01;
import org.adorsys.resource.server.serializer.DocumentGuardSerializerRegistery;
import org.adorsys.resource.server.utils.KeyStoreHandleUtils;
import org.apache.commons.lang3.RandomStringUtils;

import com.nimbusds.jose.jwk.JWKSet;

import de.adorsys.resource.server.keyservice.SecretKeyGenerator;

public class DocumentGuardService {

    private ExtendedKeystorePersistence keystorePersistence;
    private SecretKeyGenerator secretKeyGenerator;
    private ExtendedObjectPersistence objectPersistence;

    private DocumentGuardSerializerRegistery serializerRegistry = DocumentGuardSerializerRegistery.getInstance();

    public DocumentGuardService(ExtendedKeystorePersistence keystorePersistence, ExtendedObjectPersistence objectPersistence) {
        this.keystorePersistence = keystorePersistence;
        this.objectPersistence = objectPersistence;
        this.secretKeyGenerator = new SecretKeyGenerator("AES", 256);
    }

    /**
     * @param userId
     * @param userKeystoreHandler
     * @param keyPassHandler
     */
    public DocumentGuardName createUserSelfGuard(UserID userId, CallbackHandler userKeystoreHandler, CallbackHandler keyPassHandler,
                                                 BucketName keystoreBucketName, BucketName guardBucketName) {
        return createDocumentGuardAndReturnInternals(userId, userKeystoreHandler, keyPassHandler, keystoreBucketName, guardBucketName).documentGuardName;
    }


    /**
     * Loading the secret key from the guard.
     */
    public DocumentGuard loadDocumentGuard(DocumentGuardName documentGuardName, BucketName keystoreBucketName, CallbackHandler userKeystoreHandler,
                                           CallbackHandler userKeyPassHandler) {

        try {

//            ObjectHandle keystoreHandle = KeyStoreHandleUtils.userkeyStoreHandle(keystoreBucketName, documentGuardName.getUserId());
        	KeyStoreName keyStoreName = KeyStoreName.findUserKeyStoreName(keystoreBucketName, documentGuardName.getUserId());
        	KeyStore userKeystore = keystorePersistence.loadKeystore(keyStoreName, userKeystoreHandler);
//            KeyStore userKeystore = keystorePersistence.loadKeystore(keystoreHandle, userKeystoreHandler);


            // load guard file
            ObjectHandle guardHandle = new ObjectHandle(documentGuardName.getGuardBucketName().getValue(), documentGuardName.getValue());
            KeySource keySource = new KeyStoreBasedKeySourceImpl(userKeystore, userKeyPassHandler);
            PersistentObjectWrapper wrapper = objectPersistence.loadObject(guardHandle, keySource);

            ContentMetaInfo metaIno = wrapper.getMetaIno();
            Map<String, Object> addInfos = metaIno.getAddInfos();
            String serializerId = (String) addInfos.get(serializerRegistry.SERIALIZER_HEADER_KEY);
            serializerRegistry.getSerializer(serializerId);
            DocumentGuardSerializer serializer = serializerRegistry.getSerializer(serializerId);
            DocumentKey documentKey = serializer.deserializeSecretKey(wrapper.getData());

            return new DocumentGuard(documentGuardName, documentKey);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    /* This method is made for junit Tests.
     * wrapped by the public method with much less information
     */
    private DocumentGuardInternals createDocumentGuardAndReturnInternals(UserID userId, CallbackHandler userKeystoreHandler, CallbackHandler keyPassHandler,
                                                                         BucketName keystoreBucketName, BucketName guardBucketName) {

        try {
            // KeyStore laden
            ObjectHandle keystoreHandle = KeyStoreHandleUtils.userkeyStoreHandle(keystoreBucketName, userId);
        	KeyStoreName keyStoreName = KeyStoreName.findUserKeyStoreName(keystoreBucketName, userId);            
            KeyStore userKeystore = keystorePersistence.loadKeystore(keyStoreName, userKeystoreHandler);
            KeySource keySource = new KeyStoreBasedKeySourceImpl(userKeystore, keyPassHandler);

            // Willkürlich einen SecretKey aus dem KeyStore nehmen für die Verschlüsselung des Guards
            JWKSet jwkSet = JwkExport.exportKeys(userKeystore, userKeystoreHandler);
            ServerKeyMap serverKeyMap = new ServerKeyMap(jwkSet);
            KeyAndJwk randomSecretKey = serverKeyMap.randomSecretKey();
            GuardKeyID guardKeyID = new GuardKeyID(randomSecretKey.jwk.getKeyID());

            // Eine zufällige DocumentKeyID erzeugen
            DocumentKeyID documentKeyID = new DocumentKeyID(RandomStringUtils.randomAlphanumeric(20));

            // Zielpfad für den DocumentGuard bestimmen
            DocumentGuardName documentGuardName = new DocumentGuardName(guardBucketName, userId, documentKeyID);
            ObjectHandle location = new ObjectHandle(guardBucketName.getValue(), documentGuardName.getValue());

            EncryptionParams encParams = null;

            // Für die DocumentKeyID einen DocumentKey erzeugen
            SecretKeyData secretKeyData = secretKeyGenerator.generate(documentKeyID.getValue(), keyPassHandler);
            DocumentKey documentKey = new DocumentKey(secretKeyData.getSecretKey());

            // Den DocumentKey serialisieren, in der MetaInfo die SerializerID vermerken
            ContentMetaInfo metaInfo = new ContentMetaInfo();
            metaInfo.setAddInfos(new HashMap<>());
            metaInfo.getAddInfos().put(serializerRegistry.SERIALIZER_HEADER_KEY, DocumentGuardSerializer01.SERIALIZER_ID);
            byte[] serializedSecretKeyBytes = serializerRegistry.defaultSerializer().serializeSecretKey(documentKey);

            objectPersistence.storeObject(serializedSecretKeyBytes, metaInfo, location, keySource, new KeyID(guardKeyID.getValue()), encParams);

            DocumentGuardInternals documentGuardInternals = new DocumentGuardInternals(documentGuardName, documentKey, documentKeyID);
            System.out.println("========>" + documentGuardInternals);
            return documentGuardInternals;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private static class DocumentGuardInternals {
        public DocumentGuardName documentGuardName;
        public DocumentKey documentKey;
        public DocumentKeyID documentKeyID;
        public DocumentGuardInternals(DocumentGuardName documentGuardName, DocumentKey documentKey, DocumentKeyID documentKeyID) {
            this.documentGuardName = documentGuardName;
            this.documentKey = documentKey;
            this.documentKeyID = documentKeyID;
        }

        @Override
        public String toString() {
            return "DocumentGuardInternals{" +
                    "documentGuardName=" + documentGuardName +
                    ", documentKey=" + documentKey +
                    ", documentKeyID=" + documentKeyID +
                    '}';
        }
    }
}
