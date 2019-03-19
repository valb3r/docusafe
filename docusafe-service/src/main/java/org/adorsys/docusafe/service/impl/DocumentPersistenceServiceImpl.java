package org.adorsys.docusafe.service.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.service.DocumentGuardService;
import org.adorsys.docusafe.service.DocumentPersistenceService;
import org.adorsys.docusafe.service.keysource.DocumentGuardBasedKeySourceImpl;
import org.adorsys.docusafe.service.keysource.DocumentKeyIDWithKeyBasedSourceImpl;
import org.adorsys.docusafe.service.types.DocumentKey;
import org.adorsys.docusafe.service.types.DocumentKeyID;
import org.adorsys.docusafe.service.types.complextypes.DocumentBucketPath;
import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.encobject.domain.KeyStoreAccess;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.exceptions.FileExistsException;
import org.adorsys.encobject.service.api.*;
import org.adorsys.encobject.service.impl.*;
import org.adorsys.encobject.types.KeyID;
import org.adorsys.encobject.types.OverwriteFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.security.Key;
import java.security.KeyStore;

/**
 * Sample use of the encobject api to implement our protocol.
 *
 * @author fpo
 */
public class DocumentPersistenceServiceImpl implements DocumentPersistenceService {


    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentPersistenceServiceImpl.class);

    private EncryptedPersistenceService encryptedPersistenceService;
    private DocumentGuardService documentGuardService;
    private ExtendedStoreConnection extendedStoreConnection;
    private BucketServiceImpl bucketService = null;
    private DocumentKeyID2DocumentKeyCache documentKeyID2DocumentKeyCache = null;
    private KeyStoreService keyStoreService = null;

    public DocumentPersistenceServiceImpl(ExtendedStoreConnection extendedStoreConnection,
                                          DocumentKeyID2DocumentKeyCache documentKeyID2DocumentKeyCache) {
        this.extendedStoreConnection = extendedStoreConnection;
        this.encryptedPersistenceService = new EncryptedPersistenceServiceImpl(extendedStoreConnection, new AESEncryptionStreamServiceImpl());
        this.documentGuardService = new DocumentGuardServiceImpl(extendedStoreConnection);
        this.bucketService = new BucketServiceImpl(extendedStoreConnection);
        this.documentKeyID2DocumentKeyCache = documentKeyID2DocumentKeyCache;
        this.keyStoreService = new KeyStoreServiceImpl(extendedStoreConnection);
    }

    /**
     * Verschlüsselt den DocumentContent mit dem (symmetrischen) DocumentKey. Erzeugt ein Document,
     * das den verschlüsselten DocumentContent enthält. Im Header dieses Documents steht die DocumentKeyID.
     * Das Document liegt in einem Bucket mit dem Namen documentBucketPath.
     */
    @Override
    public void encryptAndPersistDocument(
            DocumentKeyIDWithKey documentKeyIDWithKey,
            DocumentBucketPath documentBucketPath,
            OverwriteFlag overwriteFlag,
            Payload payload) {

        LOGGER.debug("start encrypt and persist " + documentBucketPath);
        if (overwriteFlag.equals(OverwriteFlag.FALSE)) {
            if (bucketService.fileExists(documentBucketPath)) {
                throw new FileExistsException(documentBucketPath + " existiert und overwrite flag ist false");
            }
        }
        KeySource keySource = new DocumentKeyIDWithKeyBasedSourceImpl(documentKeyIDWithKey);
        LOGGER.debug("Document wird verschlüsselt mit " + documentKeyIDWithKey);
        KeyID keyID = new KeyID(documentKeyIDWithKey.getDocumentKeyID().getValue());
        encryptedPersistenceService.encryptAndPersist(documentBucketPath, payload, keySource, keyID);
        LOGGER.debug("finished encrypt and persist " + documentBucketPath);
    }

    @Override
    public Payload loadAndDecryptDocument(
            StorageMetadata storageMetadata,
            KeyStoreAccess keyStoreAccess,
            DocumentBucketPath documentBucketPath) {
        if (storageMetadata == null) {
            throw new BaseException("storageMetadata for load document " + documentBucketPath + " must not be null");
        }

        LOGGER.info("start load and decrypt document " + documentBucketPath + " " + keyStoreAccess);
        LOGGER.info("===========================");
        storageMetadata.getUserMetadata().keySet().forEach(key ->
            LOGGER.info(key + " -> " + storageMetadata.getUserMetadata().get(key))
        );

        DocumentKeyID documentKeyID= getDocumentKeyID(storageMetadata);
        KeySource keySource;
        if (documentKeyID.getValue().startsWith("DK")) {
            keySource = new DocumentGuardBasedKeySourceImpl(documentGuardService, keyStoreAccess, documentKeyID2DocumentKeyCache);
        } else {
            DocumentKeyIDWithKey fromCache = documentKeyID2DocumentKeyCache != null ? documentKeyID2DocumentKeyCache.get(keyStoreAccess, documentKeyID) : null;
            if (fromCache == null) {
                KeyStore userKeystore = keyStoreService.loadKeystore(keyStoreAccess.getKeyStorePath(), keyStoreAccess.getKeyStoreAuth().getReadStoreHandler());
                keySource = new KeyStoreBasedSecretKeySourceImpl(userKeystore, keyStoreAccess.getKeyStoreAuth().getReadKeyHandler());
                if (documentKeyID2DocumentKeyCache != null) {
                    SecretKey key = (SecretKey) keySource.readKey(new KeyID(documentKeyID.getValue()));
                    documentKeyID2DocumentKeyCache.put(keyStoreAccess, new DocumentKeyIDWithKey(documentKeyID, new DocumentKey(key)));
                }
            } else {
                keySource = new DocumentKeyIDWithKeyBasedSourceImpl(fromCache);
            }
        }

        Payload payload = encryptedPersistenceService.loadAndDecrypt(documentBucketPath, keySource, storageMetadata);
        LOGGER.debug("finished load and decrypt " + documentBucketPath);
        return payload;
    }

    private DocumentKeyID getDocumentKeyID(StorageMetadata storageMetadata) {
        return new DocumentKeyID(storageMetadata.getUserMetadata().get(EncryptedPersistenceServiceImpl.ENCRYPTION_KEY_ID));
    }

    @Override
    public Payload loadDocument(
            StorageMetadata storageMetadata,
            DocumentBucketPath documentBucketPath) {

        LOGGER.debug("start load document " + documentBucketPath);
        Payload blob = extendedStoreConnection.getBlob(documentBucketPath, storageMetadata);
        LOGGER.debug("finished load unencrypted Document " + documentBucketPath);
        return blob;
    }

    @Override
    public void persistDocument(DocumentBucketPath documentBucketPath, OverwriteFlag overwriteFlag, Payload payload) {
        LOGGER.debug("start persist " + documentBucketPath);
        if (overwriteFlag.equals(OverwriteFlag.FALSE)) {
            if (bucketService.fileExists(documentBucketPath)) {
                throw new FileExistsException(documentBucketPath + " existiert und overwrite flag ist false");
            }
        }

        extendedStoreConnection.putBlob(documentBucketPath, payload);
        LOGGER.debug("finished persist " + documentBucketPath);
    }

    @Override
    public void encryptAndPersistDocumentStream(
            DocumentKeyIDWithKey documentKeyIDWithKey,
            DocumentBucketPath documentBucketPath,
            OverwriteFlag overwriteFlag,
            PayloadStream payloadStream) {

        LOGGER.debug("start encrypt and persist stream " + documentBucketPath);
        if (overwriteFlag.equals(OverwriteFlag.FALSE)) {
            if (bucketService.fileExists(documentBucketPath)) {
                throw new FileExistsException(documentBucketPath + " existiert und overwrite flag ist false");
            }
        }
        KeySource keySource = new DocumentKeyIDWithKeyBasedSourceImpl(documentKeyIDWithKey);
        LOGGER.debug("Document wird verschlüsselt mit " + documentKeyIDWithKey);
        KeyID keyID = new KeyID(documentKeyIDWithKey.getDocumentKeyID().getValue());
        encryptedPersistenceService.encryptAndPersistStream(documentBucketPath, payloadStream, keySource, keyID);
        LOGGER.debug("finished encrypt and persist " + documentBucketPath);
    }

    @Override
    public PayloadStream loadAndDecryptDocumentStream(
            StorageMetadata storageMetadata,
            KeyStoreAccess keyStoreAccess,
            DocumentBucketPath documentBucketPath) {
        LOGGER.debug("start load and decrypt stream " + documentBucketPath + " " + keyStoreAccess);
        KeySource keySource = new DocumentGuardBasedKeySourceImpl(documentGuardService, keyStoreAccess, documentKeyID2DocumentKeyCache);
        PayloadStream payloadStream = encryptedPersistenceService.loadAndDecryptStream(documentBucketPath, keySource, storageMetadata);
        LOGGER.debug("finished load and decrypt stream " + documentBucketPath);
        return payloadStream;
    }

    @Override
    public PayloadStream loadDocumentStream(
            StorageMetadata storageMetadata,
            DocumentBucketPath documentBucketPath) {
        LOGGER.debug("start load stream " + documentBucketPath);
        PayloadStream payloadStream = extendedStoreConnection.getBlobStream(documentBucketPath);
        LOGGER.debug("finished load unencrypted stream " + documentBucketPath);
        return payloadStream;
    }

    @Override
    public void persistDocumentStream(DocumentBucketPath documentBucketPath, OverwriteFlag overwriteFlag, PayloadStream payloadStream) {
        LOGGER.debug("start persist stream " + documentBucketPath);
        if (overwriteFlag.equals(OverwriteFlag.FALSE)) {
            if (bucketService.fileExists(documentBucketPath)) {
                throw new FileExistsException(documentBucketPath + " existiert und overwrite flag ist false");
            }
        }
        extendedStoreConnection.putBlobStream(documentBucketPath, payloadStream);
        LOGGER.debug("finished persist " + documentBucketPath);

    }
}
