package org.adorsys.docusafe.service.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.types.MemoryContext;
import org.adorsys.docusafe.service.BucketService;
import org.adorsys.docusafe.service.DocumentGuardService;
import org.adorsys.docusafe.service.exceptions.NoDocumentGuardExists;
import org.adorsys.docusafe.service.impl.guardHelper.GuardKeyHelper;
import org.adorsys.docusafe.service.impl.guardHelper.GuardKeyHelperFactory;
import org.adorsys.docusafe.service.impl.guardHelper.KeySourceAndGuardKeyID;
import org.adorsys.docusafe.service.serializer.DocumentGuardSerializer;
import org.adorsys.docusafe.service.serializer.DocumentGuardSerializerRegistery;
import org.adorsys.docusafe.service.types.AccessType;
import org.adorsys.docusafe.service.types.DocumentKey;
import org.adorsys.docusafe.service.types.DocumentKeyID;
import org.adorsys.docusafe.service.types.GuardKey;
import org.adorsys.docusafe.service.types.complextypes.DocumentGuardLocation;
import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKeyAndAccessType;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.KeyStoreAccess;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.exceptions.FileExistsException;
import org.adorsys.encobject.service.api.EncryptedPersistenceService;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.api.KeySource;
import org.adorsys.encobject.service.api.KeystorePersistence;
import org.adorsys.encobject.service.impl.BlobStoreKeystorePersistenceImpl;
import org.adorsys.encobject.service.impl.EncryptedPersistenceServiceImpl;
import org.adorsys.encobject.service.impl.JWEncryptionStreamServiceImpl;
import org.adorsys.encobject.service.impl.KeyStoreBasedSecretKeySourceImpl;
import org.adorsys.encobject.service.impl.SimplePayloadImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
import org.adorsys.encobject.service.impl.generator.SecretKeyGeneratorImpl;
import org.adorsys.encobject.types.KeyID;
import org.adorsys.encobject.types.OverwriteFlag;
import org.adorsys.jkeygen.keystore.KeyStoreType;
import org.adorsys.jkeygen.keystore.SecretKeyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyStore;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class DocumentGuardServiceImpl implements DocumentGuardService {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentGuardServiceImpl.class);
    private final static String ACCESS_TYPE = "AccessType";
    private final static String KEYSTORE_TYPE = "KeyStoreType";
    public static final String GUARD_MAP = "GUARD_MAP";

    private KeystorePersistence keystorePersistence;
    private EncryptedPersistenceService encryptedPersistenceUtil;
    private BucketService bucketService;
    private MemoryContext memoryContext = null;


    private DocumentGuardSerializerRegistery serializerRegistry = DocumentGuardSerializerRegistery.getInstance();

    public DocumentGuardServiceImpl(ExtendedStoreConnection extendedStoreConnection) {
        this.encryptedPersistenceUtil = new EncryptedPersistenceServiceImpl(extendedStoreConnection, new JWEncryptionStreamServiceImpl());
        this.keystorePersistence = new BlobStoreKeystorePersistenceImpl(extendedStoreConnection);
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
        SecretKeyGeneratorImpl secretKeyGenerator = new SecretKeyGeneratorImpl("AES", 256);
        SecretKeyData secretKeyData = secretKeyGenerator.generate(documentKeyID.getValue(), null);
        DocumentKey documentKey = new DocumentKey(secretKeyData.getSecretKey());
        return new DocumentKeyIDWithKey(documentKeyID, documentKey);
    }

    @Override
    public void createDocumentGuardFor(GuardKeyType guardKeyType,
                                       KeyStoreAccess keyStoreAccess,
                                       DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType,
                                       OverwriteFlag overwriteFlag) {
        LOGGER.debug("start create document guard for " + documentKeyIDWithKeyAndAccessType + " at " + keyStoreAccess.getKeyStorePath());
        GuardKeyHelper helper = GuardKeyHelperFactory.getHelper(guardKeyType);
        KeySourceAndGuardKeyID keySourceAndGuardKeyID = helper.getKeySourceAndGuardKeyID(keystorePersistence, keyStoreAccess, documentKeyIDWithKeyAndAccessType);
        createDocumentGuard(keyStoreAccess, documentKeyIDWithKeyAndAccessType, keySourceAndGuardKeyID, overwriteFlag);
        LOGGER.debug("finished create document guard for " + documentKeyIDWithKeyAndAccessType + " at " + keyStoreAccess.getKeyStorePath());
    }


    /**
     * Loading the secret key from the guard.
     */
    @Override
    public DocumentKeyIDWithKeyAndAccessType loadDocumentKeyIDWithKeyAndAccessTypeFromDocumentGuard(KeyStoreAccess keyStoreAccess, DocumentKeyID documentKeyID) {
        GuardMap guardMap = memoryContext != null ? (GuardMap) memoryContext.get(GUARD_MAP) : null;
        LOGGER.debug("start load " + documentKeyID + " from document guard at " + keyStoreAccess.getKeyStorePath());
        if (guardMap != null) {
            String cacheKey = GuardMap.cacheKeyToString(keyStoreAccess, documentKeyID);
            if (guardMap.containsKey(cacheKey)) {
                return guardMap.get(cacheKey);
            }
        }

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

        LOGGER.debug("finished load " + documentKeyID + " from document guard at " + keyStoreAccess.getKeyStorePath());
        DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = new DocumentKeyIDWithKeyAndAccessType(new DocumentKeyIDWithKey(documentKeyID, documentKey), accessType);
        if (guardMap != null) {
            String cacheKey = GuardMap.cacheKeyToString(keyStoreAccess, documentKeyID);
            guardMap.put(cacheKey, documentKeyIDWithKeyAndAccessType);
        }
        return documentKeyIDWithKeyAndAccessType;
    }

    @Override
    public void setMemoryContext(MemoryContext memoryContext) {
        this.memoryContext = memoryContext;
        if (this.memoryContext != null) {
            this.memoryContext.put(GUARD_MAP, new GuardMap());
        }
    }


    private void createDocumentGuard(KeyStoreAccess keyStoreAccess,
                                     DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType,
                                     KeySourceAndGuardKeyID keySourceAndGuardKeyID,
                                     OverwriteFlag overwriteFlag) {
        LOGGER.debug("start persist document guard for " + documentKeyIDWithKeyAndAccessType + " at " + keyStoreAccess.getKeyStorePath());
        KeyStoreType keyStoreType = KeyStoreType.DEFAULT;
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
        LOGGER.debug("finished persist document guard for " + documentKeyIDWithKeyAndAccessType + " at " + keyStoreAccess.getKeyStorePath());
    }



}
