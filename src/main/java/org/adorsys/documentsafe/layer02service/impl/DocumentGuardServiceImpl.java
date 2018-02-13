package org.adorsys.documentsafe.layer02service.impl;

import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKMatcher.Builder;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.PasswordLookup;
import com.nimbusds.jose.jwk.RSAKey;
import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer02service.BucketService;
import org.adorsys.documentsafe.layer02service.DocumentGuardService;
import org.adorsys.documentsafe.layer02service.exceptions.AsymmetricEncryptionException;
import org.adorsys.documentsafe.layer02service.exceptions.NoDocumentGuardExists;
import org.adorsys.documentsafe.layer02service.exceptions.SymmetricEncryptionException;
import org.adorsys.documentsafe.layer02service.generators.SecretKeyGenerator;
import org.adorsys.documentsafe.layer02service.keysource.KeyStoreBasedPublicKeySourceImpl;
import org.adorsys.documentsafe.layer02service.keysource.KeyStoreBasedSecretKeySourceImpl;
import org.adorsys.documentsafe.layer02service.serializer.DocumentGuardSerializer;
import org.adorsys.documentsafe.layer02service.serializer.DocumentGuardSerializer01;
import org.adorsys.documentsafe.layer02service.serializer.DocumentGuardSerializerRegistery;
import org.adorsys.documentsafe.layer02service.types.DocumentKey;
import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer02service.types.GuardKey;
import org.adorsys.documentsafe.layer02service.types.GuardKeyID;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentGuardLocation;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKeyAndAccessType;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.documentsafe.layer03business.types.AccessType;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.keysource.KeySource;
import org.adorsys.encobject.params.EncryptionParams;
import org.adorsys.encobject.service.BlobStoreKeystorePersistence;
import org.adorsys.encobject.service.ExtendedStoreConnection;
import org.adorsys.encobject.service.KeystorePersistence;
import org.adorsys.encobject.service.ObjectPersistence;
import org.adorsys.encobject.service.PersistentObjectWrapper;
import org.adorsys.encobject.types.KeyID;
import org.adorsys.encobject.types.OverwriteFlag;
import org.adorsys.jjwk.keystore.JwkExport;
import org.adorsys.jjwk.serverkey.KeyAndJwk;
import org.adorsys.jjwk.serverkey.ServerKeyMap;
import org.adorsys.jkeygen.keystore.SecretKeyData;
import org.adorsys.jkeygen.utils.V3CertificateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class DocumentGuardServiceImpl implements DocumentGuardService {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentGuardServiceImpl.class);
    private final static String ACCESS_TYPE = "AccessType";

    private KeystorePersistence keystorePersistence;
    private ObjectPersistence objectPersistence;
    private BucketService bucketService;


    private DocumentGuardSerializerRegistery serializerRegistry = DocumentGuardSerializerRegistery.getInstance();

    public DocumentGuardServiceImpl(ExtendedStoreConnection extendedStoreConnection) {
        this.objectPersistence = new ObjectPersistence(extendedStoreConnection);
        this.keystorePersistence = new BlobStoreKeystorePersistence(extendedStoreConnection);
        this.bucketService = new BucketServiceImpl(extendedStoreConnection);
    }

    /**
     * erzeugt eine DocumentKeyIDWithKey
     */
    @Override
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
    @Override
    public void createSymmetricDocumentGuard(KeyStoreAccess keyStoreAccess,
                                             DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType) {
        try {
            LOGGER.info("start create symmetric encrypted document guard for " + documentKeyIDWithKeyAndAccessType + " at " + keyStoreAccess.getKeyStoreLocation());
            // KeyStore laden
            KeyStore userKeystore = keystorePersistence.loadKeystore(keyStoreAccess.getKeyStoreLocation().getLocationHandle(), keyStoreAccess.getKeyStoreAuth().getReadStoreHandler());
            KeySource keySource = new KeyStoreBasedSecretKeySourceImpl(userKeystore, keyStoreAccess.getKeyStoreAuth().getReadKeyHandler());

            // Willkürlich einen SecretKey aus dem KeyStore nehmen für die Verschlüsselung des Guards
            JWKSet jwkSet = JwkExport.exportKeys(userKeystore, keyStoreAccess.getKeyStoreAuth().getReadKeyHandler());
            if (jwkSet.getKeys().isEmpty()) {
                throw new SymmetricEncryptionException("did not find any secret keys in keystore with id: " + keyStoreAccess.getKeyStoreLocation().getKeyStoreID());
            }
            ServerKeyMap serverKeyMap = new ServerKeyMap(jwkSet);
            KeyAndJwk randomSecretKey = serverKeyMap.randomSecretKey();
            GuardKeyID guardKeyID = new GuardKeyID(randomSecretKey.jwk.getKeyID());
            LOGGER.debug("Guard created with symmetric KeyID :" + guardKeyID);

            // Zielpfad für den DocumentKeyIDWithKey bestimmen
            ObjectHandle documentGuardHandle = DocumentGuardLocation.getLocationHandle(keyStoreAccess.getKeyStoreLocation(),
                    documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKeyID());
            EncryptionParams encParams = null;

            // Den DocumentKey serialisieren, in der MetaInfo die SerializerID vermerken
            ContentMetaInfo metaInfo = new ContentMetaInfo();
            metaInfo.setAddInfos(new HashMap<>());
            metaInfo.getAddInfos().put(serializerRegistry.SERIALIZER_HEADER_KEY, DocumentGuardSerializer01.SERIALIZER_ID);
            metaInfo.getAddInfos().put(ACCESS_TYPE, documentKeyIDWithKeyAndAccessType.getAccessType());
            GuardKey guardKey = new GuardKey(serializerRegistry.defaultSerializer().serializeSecretKey(documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKey()));

            objectPersistence.storeObject(guardKey.getValue(), metaInfo, documentGuardHandle, keySource, new KeyID(guardKeyID.getValue()), encParams, OverwriteFlag.FALSE);
            LOGGER.info("finished create symmetric encrypted document guard for " + documentKeyIDWithKeyAndAccessType + " at " + keyStoreAccess.getKeyStoreLocation());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }


    /**
     * holt sich aus dem KeyStore einen beliebigen PublicKey, mit dem der übergebene DocumentKey asymmetrisch veschlüsselt wird
     * Dort, wo der KeyStore liegt wird dann ein DocumentGuard erzeugt, der den verschlüsselten DocumentKey enthält.
     * Im Header des DocumentGuards steht die DocuemntKeyID.
     */
    @Override
    public void createAsymmetricDocumentGuard(KeyStoreAccess receiverKeyStoreAccess,
                                              DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType,
                                              OverwriteFlag overwriteFlag) {
        try {
            LOGGER.info("start create asymmetric encrypted document guard for " + documentKeyIDWithKeyAndAccessType + " at " + receiverKeyStoreAccess.getKeyStoreLocation());
            KeyStore userKeystore = keystorePersistence.loadKeystore(receiverKeyStoreAccess.getKeyStoreLocation().getLocationHandle(), receiverKeyStoreAccess.getKeyStoreAuth().getReadStoreHandler());

            JWKSet exportKeys = load(userKeystore, null);
            LOGGER.debug("number of public keys found:" + exportKeys.getKeys().size());
            List<JWK> encKeys = selectEncKeys(exportKeys);
            if (encKeys.isEmpty()) {
                throw new AsymmetricEncryptionException("did not find any public keys in keystore with id: " + receiverKeyStoreAccess.getKeyStoreLocation().getKeyStoreID());
            }
            JWK randomKey = JwkExport.randomKey(encKeys);
            GuardKeyID guardKeyID = new GuardKeyID(randomKey.getKeyID());
            LOGGER.debug("Guard created with asymmetric KeyID :" + guardKeyID);

            KeySource keySource = new KeyStoreBasedPublicKeySourceImpl(exportKeys);

            // Zielpfad für den DocumentKeyIDWithKey bestimmen
            ObjectHandle documentGuardHandle = DocumentGuardLocation.getLocationHandle(receiverKeyStoreAccess.getKeyStoreLocation(),
                    documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKeyID());
            EncryptionParams encParams = null;

            // Den DocumentKey serialisieren, in der MetaInfo die SerializerID vermerken
            ContentMetaInfo metaInfo = new ContentMetaInfo();
            metaInfo.setAddInfos(new HashMap<>());
            metaInfo.getAddInfos().put(serializerRegistry.SERIALIZER_HEADER_KEY, DocumentGuardSerializer01.SERIALIZER_ID);
            metaInfo.getAddInfos().put(ACCESS_TYPE, documentKeyIDWithKeyAndAccessType.getAccessType().toString());
            GuardKey guardKey = new GuardKey(serializerRegistry.defaultSerializer().serializeSecretKey(documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKey()));

            objectPersistence.storeObject(guardKey.getValue(), metaInfo, documentGuardHandle, keySource, new KeyID(guardKeyID.getValue()), encParams, overwriteFlag);
            LOGGER.info("finished create asymmetric encrypted document guard for " + documentKeyIDWithKeyAndAccessType + " at " + receiverKeyStoreAccess.getKeyStoreLocation());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    /**
     * Loading the secret key from the guard.
     */
    @Override
    public DocumentKeyIDWithKeyAndAccessType loadDocumentKeyIDWithKeyAndAccessTypeFromDocumentGuard(KeyStoreAccess keyStoreAccess, DocumentKeyID documentKeyID) {
        try {
            LOGGER.info("start load " + documentKeyID + " from document guard at " + keyStoreAccess.getKeyStoreLocation());

            KeyStore userKeystore = keystorePersistence.loadKeystore(keyStoreAccess.getKeyStoreLocation().getLocationHandle(), keyStoreAccess.getKeyStoreAuth().getReadStoreHandler());

            // load guard file
            KeySource keySource = new KeyStoreBasedSecretKeySourceImpl(userKeystore, keyStoreAccess.getKeyStoreAuth().getReadKeyHandler());
            BucketPath guardBucketPath = DocumentGuardLocation.getBucketPathOfGuard(keyStoreAccess.getKeyStoreLocation(), documentKeyID);
            if (!bucketService.existsFile(guardBucketPath)) {
                throw new NoDocumentGuardExists(guardBucketPath);
            }
            LOGGER.debug("loadDocumentKey for " + guardBucketPath);
            PersistentObjectWrapper wrapper = objectPersistence.loadObject(guardBucketPath.getObjectHandle(), keySource);
            ContentMetaInfo metaIno = wrapper.getMetaIno();
            Map<String, Object> addInfos = metaIno.getAddInfos();
            String accesstypestring = (String) addInfos.get(ACCESS_TYPE);
            if (accesstypestring == null) {
                throw new BaseException("PROGRAMMING ERROR. AcessType for Guard with KeyID " + documentKeyID + " not known");
            }
            AccessType accessType = AccessType.WRITE.valueOf(accesstypestring);
            String serializerId = (String) addInfos.get(serializerRegistry.SERIALIZER_HEADER_KEY);
            serializerRegistry.getSerializer(serializerId);
            DocumentGuardSerializer serializer = serializerRegistry.getSerializer(serializerId);
            DocumentKey documentKey = serializer.deserializeSecretKey(wrapper.getData());

            LOGGER.info("finished load " + documentKeyID + " from document guard at " + keyStoreAccess.getKeyStoreLocation());
            return new DocumentKeyIDWithKeyAndAccessType(new DocumentKeyIDWithKey(documentKeyID, documentKey), accessType);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }


    private static List<JWK> selectEncKeys(JWKSet exportKeys) {
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

                    // Let keyID=alias
                    // Converting from a certificate, the id is set as the thumbprint of the certificate.
                    rsaJWK = new RSAKey.Builder(rsaJWK).keyID(keyAlias).keyStore(keyStore).build();
                    jwks.add(rsaJWK);

                } else if (cert.getPublicKey() instanceof ECPublicKey) {
                    List<X509Certificate> convertedCert = V3CertificateUtils.convert(certs);
                    ECKey ecJWK = ECKey.parse(convertedCert.get(0));

                    // Let keyID=alias
                    // Converting from a certificate, the id is set as the thumbprint of the certificate.
                    ecJWK = new ECKey.Builder(ecJWK).keyID(keyAlias).keyStore(keyStore).build();
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

}
