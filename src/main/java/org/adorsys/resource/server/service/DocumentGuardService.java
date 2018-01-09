package org.adorsys.resource.server.service;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKMatcher.Builder;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import de.adorsys.resource.server.keyservice.SecretKeyGenerator;
import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.params.EncryptionParams;
import org.adorsys.jjwk.keystore.JwkExport;
import org.adorsys.jjwk.serverkey.KeyAndJwk;
import org.adorsys.jjwk.serverkey.ServerKeyMap;
import org.adorsys.jkeygen.keystore.SecretKeyData;
import org.adorsys.resource.server.exceptions.BaseException;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.ExtendedKeystorePersistence;
import org.adorsys.resource.server.persistence.ExtendedObjectPersistence;
import org.adorsys.resource.server.persistence.KeySource;
import org.adorsys.resource.server.persistence.KeyStoreBasedPublicKeySourceImpl;
import org.adorsys.resource.server.persistence.KeyStoreBasedSecretKeySourceImpl;
import org.adorsys.resource.server.persistence.PersistentObjectWrapper;
import org.adorsys.resource.server.persistence.basetypes.DocumentKey;
import org.adorsys.resource.server.persistence.basetypes.DocumentKeyID;
import org.adorsys.resource.server.persistence.basetypes.GuardKey;
import org.adorsys.resource.server.persistence.basetypes.GuardKeyID;
import org.adorsys.resource.server.persistence.basetypes.KeyID;
import org.adorsys.resource.server.persistence.complextypes.DocumentGuardLocation;
import org.adorsys.resource.server.persistence.complextypes.DocumentKeyIDWithKey;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreAccess;
import org.adorsys.resource.server.serializer.DocumentGuardSerializer;
import org.adorsys.resource.server.serializer.DocumentGuardSerializer01;
import org.adorsys.resource.server.serializer.DocumentGuardSerializerRegistery;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DocumentGuardService {

    private ExtendedKeystorePersistence keystorePersistence;
    private ExtendedObjectPersistence objectPersistence;

    private DocumentGuardSerializerRegistery serializerRegistry = DocumentGuardSerializerRegistery.getInstance();

    public DocumentGuardService(ExtendedKeystorePersistence keystorePersistence, ExtendedObjectPersistence objectPersistence) {
        this.keystorePersistence = keystorePersistence;
        this.objectPersistence = objectPersistence;
    }

    /**
     * erzeugt eine DocumentKeyIDWithKey
     */
    public DocumentKeyIDWithKey createDocumentKeyIdWithKey() {
        try {
            // Eine zufällige DocumentKeyID erzeugen
            DocumentKeyID documentKeyID = new DocumentKeyID("DK" + UUID.randomUUID().toString());

            // Für die DocumentKeyID einen DocumentKey erzeugen
            SecretKeyGenerator secretKeyGenerator = new SecretKeyGenerator("AES", 256);
            SecretKeyData secretKeyData = secretKeyGenerator.generate(documentKeyID.getValue(), null);
            DocumentKey documentKey = new DocumentKey(secretKeyData.getSecretKey());
            return new DocumentKeyIDWithKey(documentKeyID, documentKey);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    /**
     * holt sich aus dem KeyStore einen beliebigen SecretKey, mit dem der übergebene DocumentKey symmetrisch veschlüsselt wird
     * Dort, wo der KeyStore liegt wird dann ein DocumentGuard erzeugt, der den verschlüsselten DocumentKey enthält.
     * Im Header des DocumentGuards steht die DocuemntKeyID.
     */
    public void createSymmetricDocumentGuard(KeyStoreAccess keyStoreAccess, DocumentKeyIDWithKey documentKeyIDWithKey) {
        try {
            // KeyStore laden
            KeyStore userKeystore = keystorePersistence.loadKeystore(keyStoreAccess.getKeyStoreLocation(), keyStoreAccess.getKeyStoreAuth().getReadStoreHandler());
            KeySource keySource = new KeyStoreBasedSecretKeySourceImpl(userKeystore, keyStoreAccess.getKeyStoreAuth().getReadKeyHandler());

            // Willkürlich einen SecretKey aus dem KeyStore nehmen für die Verschlüsselung des Guards
            JWKSet jwkSet = JwkExport.exportKeys(userKeystore, keyStoreAccess.getKeyStoreAuth().getReadKeyHandler());
            ServerKeyMap serverKeyMap = new ServerKeyMap(jwkSet);
            KeyAndJwk randomSecretKey = serverKeyMap.randomSecretKey();
            GuardKeyID guardKeyID = new GuardKeyID(randomSecretKey.jwk.getKeyID());

            // Zielpfad für den DocumentKeyIDWithKey bestimmen
            ObjectHandle documentGuardHandle = DocumentGuardLocation.getLocationHandle(keyStoreAccess.getKeyStoreLocation(), documentKeyIDWithKey.getDocumentKeyID());
            EncryptionParams encParams = null;

            // Den DocumentKey serialisieren, in der MetaInfo die SerializerID vermerken
            ContentMetaInfo metaInfo = new ContentMetaInfo();
            metaInfo.setAddInfos(new HashMap<>());
            metaInfo.getAddInfos().put(serializerRegistry.SERIALIZER_HEADER_KEY, DocumentGuardSerializer01.SERIALIZER_ID);
            GuardKey guardKey = new GuardKey(serializerRegistry.defaultSerializer().serializeSecretKey(documentKeyIDWithKey.getDocumentKey()));

            objectPersistence.storeObject(guardKey.getValue(), metaInfo, documentGuardHandle, keySource, new KeyID(guardKeyID.getValue()), encParams);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }


    /**
     * holt sich aus dem KeyStore einen beliebigen PublicKey, mit dem der übergebene DocumentKey asymmetrisch veschlüsselt wird
     * Dort, wo der KeyStore liegt wird dann ein DocumentGuard erzeugt, der den verschlüsselten DocumentKey enthält.
     * Im Header des DocumentGuards steht die DocuemntKeyID.
     */
    public void createAsymmetricDocumentGuard(KeyStoreAccess receiverKeyStoreAccess, DocumentKeyIDWithKey documentKeyIDWithKey) {
        try {
            KeyStore userKeystore = keystorePersistence.loadKeystore(receiverKeyStoreAccess.getKeyStoreLocation(), receiverKeyStoreAccess.getKeyStoreAuth().getReadStoreHandler());

            /**
             * JWKSet exportKeys = JwkExport.exportKeys(userKeystore, null); // Geht nicht mit null Parameter
             */
            JWKSet exportKeys = JWKSet.load(userKeystore, null);
            System.out.println("exportKeys # " + exportKeys.getKeys().size());
            List<JWK> encKeys = selectEncKeys(exportKeys);
            if (encKeys.isEmpty()) {
                throw new BaseException("did not find any public keys in keystore with id: " + receiverKeyStoreAccess.getKeyStoreLocation().getKeyStoreID());
            }
            JWK randomKey = JwkExport.randomKey(encKeys);
            String randomEncKeyId = randomKey.getKeyID();


            KeySource keySource = new KeyStoreBasedPublicKeySourceImpl(exportKeys);
            GuardKeyID guardKeyID = new GuardKeyID(randomEncKeyId);

            // Zielpfad für den DocumentKeyIDWithKey bestimmen
            ObjectHandle documentGuardHandle = DocumentGuardLocation.getLocationHandle(receiverKeyStoreAccess.getKeyStoreLocation(), documentKeyIDWithKey.getDocumentKeyID());
            EncryptionParams encParams = new EncryptionParams();
            encParams.setEncAlgo(JWEAlgorithm.RSA1_5);
            encParams.setEncMethod(EncryptionMethod.A256GCM);

            // Den DocumentKey serialisieren, in der MetaInfo die SerializerID vermerken
            ContentMetaInfo metaInfo = new ContentMetaInfo();
            metaInfo.setAddInfos(new HashMap<>());
            metaInfo.getAddInfos().put(serializerRegistry.SERIALIZER_HEADER_KEY, DocumentGuardSerializer01.SERIALIZER_ID);
            GuardKey guardKey = new GuardKey(serializerRegistry.defaultSerializer().serializeSecretKey(documentKeyIDWithKey.getDocumentKey()));

            objectPersistence.storeObject(guardKey.getValue(), metaInfo, documentGuardHandle, keySource, new KeyID(guardKeyID.getValue()), encParams);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public static List<JWK> selectEncKeys(JWKSet exportKeys) {
        JWKMatcher signKeys = (new Builder()).keyUse(KeyUse.ENCRYPTION).build();
        return (new JWKSelector(signKeys)).select(exportKeys);
    }


    /**
     * Loading the secret key from the guard.
     */
    public DocumentKeyIDWithKey loadDocumentKeyIDWithKeyFromDocumentGuard(KeyStoreAccess keyStoreAccess, DocumentKeyID documentKeyID) {

        try {

            KeyStore userKeystore = keystorePersistence.loadKeystore(keyStoreAccess.getKeyStoreLocation(), keyStoreAccess.getKeyStoreAuth().getReadStoreHandler());

            // load guard file
            KeySource keySource = new KeyStoreBasedSecretKeySourceImpl(userKeystore, keyStoreAccess.getKeyStoreAuth().getReadKeyHandler());
            PersistentObjectWrapper wrapper = objectPersistence.loadObject(DocumentGuardLocation.getLocationHandle(keyStoreAccess.getKeyStoreLocation(), documentKeyID), keySource);

            ContentMetaInfo metaIno = wrapper.getMetaIno();
            Map<String, Object> addInfos = metaIno.getAddInfos();
            String serializerId = (String) addInfos.get(serializerRegistry.SERIALIZER_HEADER_KEY);
            serializerRegistry.getSerializer(serializerId);
            DocumentGuardSerializer serializer = serializerRegistry.getSerializer(serializerId);
            DocumentKey documentKey = serializer.deserializeSecretKey(wrapper.getData());

            return new DocumentKeyIDWithKey(documentKeyID, documentKey);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
