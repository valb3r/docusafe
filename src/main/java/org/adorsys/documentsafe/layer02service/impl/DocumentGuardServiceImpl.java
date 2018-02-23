package org.adorsys.documentsafe.layer02service.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.documentsafe.layer02service.BucketService;
import org.adorsys.documentsafe.layer02service.DocumentGuardService;
import org.adorsys.documentsafe.layer02service.exceptions.NoDocumentGuardExists;
import org.adorsys.documentsafe.layer02service.generators.SecretKeyGenerator;
import org.adorsys.documentsafe.layer02service.impl.guardHelper.GuardKeyHelper;
import org.adorsys.documentsafe.layer02service.impl.guardHelper.GuardKeyHelperFactory;
import org.adorsys.documentsafe.layer02service.impl.guardHelper.KeySourceAndGuardKeyID;
import org.adorsys.documentsafe.layer02service.serializer.DocumentGuardSerializer;
import org.adorsys.documentsafe.layer02service.serializer.DocumentGuardSerializerRegistery;
import org.adorsys.documentsafe.layer02service.types.DocumentKey;
import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer02service.types.GuardKey;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentGuardLocation;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKeyAndAccessType;
import org.adorsys.documentsafe.layer03business.types.AccessType;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.KeyStoreAccess;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.exceptions.FileExistsException;
import org.adorsys.encobject.keysource.KeySource;
import org.adorsys.encobject.keysource.KeyStoreBasedSecretKeySourceImpl;
import org.adorsys.encobject.service.BlobStoreKeystorePersistence;
import org.adorsys.encobject.service.EncryptedPersistenceService;
import org.adorsys.encobject.service.ExtendedStoreConnection;
import org.adorsys.encobject.service.JWEncryptionService;
import org.adorsys.encobject.service.KeystorePersistence;
import org.adorsys.encobject.service.SimplePayloadImpl;
import org.adorsys.encobject.service.SimpleStorageMetadataImpl;
import org.adorsys.encobject.types.KeyID;
import org.adorsys.encobject.types.KeyStoreType;
import org.adorsys.encobject.types.OverwriteFlag;
import org.adorsys.jkeygen.keystore.SecretKeyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyStore;
import java.util.UUID;

public class DocumentGuardServiceImpl implements DocumentGuardService {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentGuardServiceImpl.class);
    private final static String ACCESS_TYPE = "AccessType";
    private final static String KEYSTORE_TYPE = "KeyStoreType";

    private KeystorePersistence keystorePersistence;
    private EncryptedPersistenceService encryptedPersistenceUtil;
    private BucketService bucketService;


    private DocumentGuardSerializerRegistery serializerRegistry = DocumentGuardSerializerRegistery.getInstance();

    public DocumentGuardServiceImpl(ExtendedStoreConnection extendedStoreConnection) {
        this.encryptedPersistenceUtil = new EncryptedPersistenceService(extendedStoreConnection, new JWEncryptionService());
        this.keystorePersistence = new BlobStoreKeystorePersistence(extendedStoreConnection);
        this.bucketService = new BucketServiceImpl(extendedStoreConnection);
    }

    /**
     * erzeugt eine DocumentKeyIDWithKey
     */
    @Override
    public DocumentKeyIDWithKey createDocumentKeyIdWithKey() {
        // Eine zufällige DocumentKeyID erzeugen
        DocumentKeyID documentKeyID = new DocumentKeyID("DK" + UUID.randomUUID().toString());

        // Für die DocumentKeyID einen DocumentKey erzeugen
        SecretKeyGenerator secretKeyGenerator = new SecretKeyGenerator("AES", 256);
        SecretKeyData secretKeyData = secretKeyGenerator.generate(documentKeyID.getValue(), null);
        DocumentKey documentKey = new DocumentKey(secretKeyData.getSecretKey());
        return new DocumentKeyIDWithKey(documentKeyID, documentKey);
    }

    @Override
    public void createDocumentGuardFor(GuardKeyType guardKeyType,
                                       KeyStoreAccess keyStoreAccess,
                                       DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType,
                                       OverwriteFlag overwriteFlag) {
        LOGGER.info("start create document guard for " + documentKeyIDWithKeyAndAccessType + " at " + keyStoreAccess.getKeyStorePath());
        GuardKeyHelper helper = GuardKeyHelperFactory.getHelper(guardKeyType);
        KeySourceAndGuardKeyID keySourceAndGuardKeyID = helper.getKeySourceAndGuardKeyID(keystorePersistence, keyStoreAccess, documentKeyIDWithKeyAndAccessType);
        createDocumentGuard(keyStoreAccess, documentKeyIDWithKeyAndAccessType, keySourceAndGuardKeyID, overwriteFlag);
        LOGGER.info("finished create document guard for " + documentKeyIDWithKeyAndAccessType + " at " + keyStoreAccess.getKeyStorePath());
    }


    /**
     * Loading the secret key from the guard.
     */
    @Override
    public DocumentKeyIDWithKeyAndAccessType loadDocumentKeyIDWithKeyAndAccessTypeFromDocumentGuard(KeyStoreAccess keyStoreAccess, DocumentKeyID documentKeyID) {
        LOGGER.info("start load " + documentKeyID + " from document guard at " + keyStoreAccess.getKeyStorePath());

        KeyStore userKeystore = keystorePersistence.loadKeystore(keyStoreAccess.getKeyStorePath().getObjectHandle(), keyStoreAccess.getKeyStoreAuth().getReadStoreHandler());

        // load guard file
        KeySource keySource = new KeyStoreBasedSecretKeySourceImpl(userKeystore, keyStoreAccess.getKeyStoreAuth().getReadKeyHandler());
        BucketPath guardBucketPath = DocumentGuardLocation.getBucketPathOfGuard(keyStoreAccess.getKeyStorePath(), documentKeyID);
        if (!bucketService.fileExists(guardBucketPath)) {
            throw new NoDocumentGuardExists(guardBucketPath);
        }
        LOGGER.debug("loadDocumentKey for " + guardBucketPath);
        Payload payload = encryptedPersistenceUtil.loadAndDecrypt(guardBucketPath, keySource);
        String accesstypestring = payload.getStorageMetadata().getUserMetadata().get(ACCESS_TYPE);
        if (accesstypestring == null) {
            throw new BaseException("PROGRAMMING ERROR. AccessType for Guard with KeyID " + documentKeyID + " not known");
        }
        String keyStoreTypeString = payload.getStorageMetadata().getUserMetadata().get(KEYSTORE_TYPE);
        if (keyStoreTypeString == null) {
            throw new BaseException("PROGRAMMING ERROR. KeyStoreType for Guard with KeyID " + documentKeyID + " not known");
        }
        KeyStoreType keyStoreType = new KeyStoreType(keyStoreTypeString);

        AccessType accessType = AccessType.WRITE.valueOf(accesstypestring);
        String serializerId = payload.getStorageMetadata().getUserMetadata().get(serializerRegistry.SERIALIZER_HEADER_KEY);
        DocumentGuardSerializer serializer = serializerRegistry.getSerializer(serializerId);
        DocumentKey documentKey = serializer.deserializeSecretKey(payload.getData(), keyStoreType);

        LOGGER.info("finished load " + documentKeyID + " from document guard at " + keyStoreAccess.getKeyStorePath());
        return new DocumentKeyIDWithKeyAndAccessType(new DocumentKeyIDWithKey(documentKeyID, documentKey), accessType);
    }


    private void createDocumentGuard(KeyStoreAccess keyStoreAccess,
                                     DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType,
                                     KeySourceAndGuardKeyID keySourceAndGuardKeyID,
                                     OverwriteFlag overwriteFlag) {
        LOGGER.info("start persist document guard for " + documentKeyIDWithKeyAndAccessType + " at " + keyStoreAccess.getKeyStorePath());
        KeyStoreType keyStoreType = new KeyStoreType("UBER");
        BucketPath documentGuardBucketPath = DocumentGuardLocation.getBucketPathOfGuard(keyStoreAccess.getKeyStorePath(),
                documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKeyID());
        if (overwriteFlag.equals(OverwriteFlag.FALSE)) {
            if (bucketService.fileExists(documentGuardBucketPath)) {
                throw new FileExistsException("File " + documentGuardBucketPath + " already exists and overwriteflag is false");
            }
        }

        // Den DocumentKey serialisieren, in der MetaInfo die SerializerID vermerken
        SimpleStorageMetadataImpl storageMetadata = new SimpleStorageMetadataImpl();
        DocumentGuardSerializer documentGuardSerializer = serializerRegistry.defaultSerializer();
        storageMetadata.getUserMetadata().put(serializerRegistry.SERIALIZER_HEADER_KEY, documentGuardSerializer.getSerializerID());
        storageMetadata.getUserMetadata().put(ACCESS_TYPE, documentKeyIDWithKeyAndAccessType.getAccessType().toString());
        storageMetadata.getUserMetadata().put(KEYSTORE_TYPE, keyStoreType.getValue());
        GuardKey guardKey = new GuardKey(documentGuardSerializer.serializeSecretKey(
                documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKey(), keyStoreType));

        Payload payload = new SimplePayloadImpl(storageMetadata, guardKey.getValue());

        encryptedPersistenceUtil.encryptAndPersist(documentGuardBucketPath, payload, keySourceAndGuardKeyID.keySource,
                new KeyID(keySourceAndGuardKeyID.guardKeyID.getValue()));
        // TODO OverwriteFlag
        LOGGER.info("finished persist document guard for " + documentKeyIDWithKeyAndAccessType + " at " + keyStoreAccess.getKeyStorePath());
    }


}
