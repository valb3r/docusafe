package org.adorsys.resource.server.service;

import com.nimbusds.jose.jwk.JWKSet;
import de.adorsys.resource.server.keyservice.SecretKeyGenerator;
import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.params.EncryptionParams;
import org.adorsys.jjwk.keystore.JwkExport;
import org.adorsys.jjwk.serverkey.KeyAndJwk;
import org.adorsys.jjwk.serverkey.ServerKeyMap;
import org.adorsys.jkeygen.keystore.SecretKeyData;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.ExtendedKeystorePersistence;
import org.adorsys.resource.server.persistence.ExtendedObjectPersistence;
import org.adorsys.resource.server.persistence.KeySource;
import org.adorsys.resource.server.persistence.KeyStoreBasedKeySourceImpl;
import org.adorsys.resource.server.persistence.PersistentObjectWrapper;
import org.adorsys.resource.server.persistence.basetypes.DocumentKey;
import org.adorsys.resource.server.persistence.basetypes.DocumentKeyID;
import org.adorsys.resource.server.persistence.basetypes.GuardKey;
import org.adorsys.resource.server.persistence.basetypes.GuardKeyID;
import org.adorsys.resource.server.persistence.basetypes.KeyID;
import org.adorsys.resource.server.persistence.complextypes.DocumentGuard;
import org.adorsys.resource.server.persistence.complextypes.DocumentGuardLocation;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreAccess;
import org.adorsys.resource.server.serializer.DocumentGuardSerializer;
import org.adorsys.resource.server.serializer.DocumentGuardSerializer01;
import org.adorsys.resource.server.serializer.DocumentGuardSerializerRegistery;
import org.apache.commons.lang3.RandomStringUtils;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

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

    public DocumentKeyID createDocumentGuard(KeyStoreAccess keyStoreAccess) {
        try {
            // KeyStore laden
            KeyStore userKeystore = keystorePersistence.loadKeystore(keyStoreAccess.getKeyStoreLocation(), keyStoreAccess.getKeyStoreAuth().getUserpass());
            KeySource keySource = new KeyStoreBasedKeySourceImpl(userKeystore, keyStoreAccess.getKeyStoreAuth().getKeypass());

            // Willkürlich einen SecretKey aus dem KeyStore nehmen für die Verschlüsselung des Guards
            JWKSet jwkSet = JwkExport.exportKeys(userKeystore, keyStoreAccess.getKeyStoreAuth().getKeypass());
            ServerKeyMap serverKeyMap = new ServerKeyMap(jwkSet);
            KeyAndJwk randomSecretKey = serverKeyMap.randomSecretKey();
            GuardKeyID guardKeyID = new GuardKeyID(randomSecretKey.jwk.getKeyID());

            // Eine zufällige DocumentKeyID erzeugen
            DocumentKeyID documentKeyID = new DocumentKeyID(RandomStringUtils.randomAlphanumeric(20));

            // Zielpfad für den DocumentGuard bestimmen
            ObjectHandle documentGuardHandle = DocumentGuardLocation.getLocationHandle(keyStoreAccess.getKeyStoreLocation(), documentKeyID);
            EncryptionParams encParams = null;

            // Für die DocumentKeyID einen DocumentKey erzeugen
            SecretKeyData secretKeyData = secretKeyGenerator.generate(documentKeyID.getValue(), keyStoreAccess.getKeyStoreAuth().getKeypass());
            DocumentKey documentKey = new DocumentKey(secretKeyData.getSecretKey());

            // Den DocumentKey serialisieren, in der MetaInfo die SerializerID vermerken
            ContentMetaInfo metaInfo = new ContentMetaInfo();
            metaInfo.setAddInfos(new HashMap<>());
            metaInfo.getAddInfos().put(serializerRegistry.SERIALIZER_HEADER_KEY, DocumentGuardSerializer01.SERIALIZER_ID);
            GuardKey guardKey = new GuardKey(serializerRegistry.defaultSerializer().serializeSecretKey(documentKey));

            objectPersistence.storeObject(guardKey.getValue(), metaInfo, documentGuardHandle, keySource, new KeyID(guardKeyID.getValue()), encParams);
            return documentKeyID;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    /**
     * Loading the secret key from the guard.
     */
    public DocumentGuard loadDocumentGuard(KeyStoreAccess keyStoreAccess, DocumentKeyID documentKeyID) {

        try {

        	KeyStore userKeystore = keystorePersistence.loadKeystore(keyStoreAccess.getKeyStoreLocation(), keyStoreAccess.getKeyStoreAuth().getUserpass());

            // load guard file
            KeySource keySource = new KeyStoreBasedKeySourceImpl(userKeystore, keyStoreAccess.getKeyStoreAuth().getKeypass());
            PersistentObjectWrapper wrapper = objectPersistence.loadObject(DocumentGuardLocation.getLocationHandle(keyStoreAccess.getKeyStoreLocation(), documentKeyID), keySource);

            ContentMetaInfo metaIno = wrapper.getMetaIno();
            Map<String, Object> addInfos = metaIno.getAddInfos();
            String serializerId = (String) addInfos.get(serializerRegistry.SERIALIZER_HEADER_KEY);
            serializerRegistry.getSerializer(serializerId);
            DocumentGuardSerializer serializer = serializerRegistry.getSerializer(serializerId);
            DocumentKey documentKey = serializer.deserializeSecretKey(wrapper.getData());

            return new DocumentGuard(documentKeyID, documentKey);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
