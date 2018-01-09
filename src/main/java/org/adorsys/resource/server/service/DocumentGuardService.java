package org.adorsys.resource.server.service;

import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKMatcher.Builder;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.PasswordLookup;
import com.nimbusds.jose.jwk.RSAKey;
import de.adorsys.resource.server.keyservice.SecretKeyGenerator;
import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.params.EncryptionParams;
import org.adorsys.jjwk.keystore.JwkExport;
import org.adorsys.jjwk.serverkey.KeyAndJwk;
import org.adorsys.jjwk.serverkey.ServerKeyMap;
import org.adorsys.jkeygen.keystore.SecretKeyData;
import org.adorsys.jkeygen.utils.V3CertificateUtils;
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
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
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
            System.out.println("Guard created with symmetric KeyID :" + guardKeyID);

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

            JWKSet exportKeys = load(userKeystore, null);
            System.out.println("exportKeys # " + exportKeys.getKeys().size());
            List<JWK> encKeys = selectEncKeys(exportKeys);
            if (encKeys.isEmpty()) {
                throw new BaseException("did not find any public keys in keystore with id: " + receiverKeyStoreAccess.getKeyStoreLocation().getKeyStoreID());
            }
            JWK randomKey = JwkExport.randomKey(encKeys);
            GuardKeyID guardKeyID = new GuardKeyID(randomKey.getKeyID());
            System.out.println("Guard created with asymmetric KeyID :" + guardKeyID);

            KeySource keySource = new KeyStoreBasedPublicKeySourceImpl(exportKeys);

            // Zielpfad für den DocumentKeyIDWithKey bestimmen
            ObjectHandle documentGuardHandle = DocumentGuardLocation.getLocationHandle(receiverKeyStoreAccess.getKeyStoreLocation(), documentKeyIDWithKey.getDocumentKeyID());
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

    public static List<JWK> selectEncKeys(JWKSet exportKeys) {
        JWKMatcher signKeys = (new Builder()).keyUse(KeyUse.ENCRYPTION).build();
        return (new JWKSelector(signKeys)).select(exportKeys);
    }

        private JWKSet load(final KeyStore keyStore, final PasswordLookup pwLookup)
		throws KeyStoreException {
            try {

                List<JWK> jwks = new LinkedList<>();

                // Load RSA and EC keys
                for (Enumeration<String> keyAliases = keyStore.aliases(); keyAliases.hasMoreElements(); ) {

                    final String keyAlias = keyAliases.nextElement();
                    final char[] keyPassword = pwLookup == null ? "".toCharArray() : pwLookup.lookupPassword(keyAlias);

                    Certificate cert = keyStore.getCertificate(keyAlias);
                    if (cert == null) {
                        continue; // skip
                    }

                    Certificate[] certs = new Certificate[]{cert};
                    if (cert.getPublicKey() instanceof RSAPublicKey) {
                        List<X509Certificate> convertedCert = V3CertificateUtils.convert(certs);
                        RSAKey rsaJWK = RSAKey.parse(convertedCert.get(0));

                        jwks.add(rsaJWK);

                    } else if (cert.getPublicKey() instanceof ECPublicKey) {
                        List<X509Certificate> convertedCert = V3CertificateUtils.convert(certs);
                        ECKey ecJWK = ECKey.parse(convertedCert.get(0));

                        jwks.add(ecJWK);
                    } else {
                        continue;
                    }
                }
                return new JWKSet(jwks);
            } catch (Exception e) {
                throw BaseExceptionHandler.handle(e);
            }
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
