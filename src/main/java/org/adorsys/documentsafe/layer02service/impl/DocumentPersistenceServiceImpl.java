package org.adorsys.documentsafe.layer02service.impl;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer02service.DocumentGuardService;
import org.adorsys.documentsafe.layer02service.DocumentPersistenceService;
import org.adorsys.documentsafe.layer02service.keysource.DocumentGuardBasedKeySourceImpl;
import org.adorsys.documentsafe.layer02service.keysource.DocumentKeyIDWithKeyBasedSourceImpl;
import org.adorsys.documentsafe.layer02service.types.DocumentContent;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentBucketPath;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.exceptions.FileExistsException;
import org.adorsys.encobject.keysource.KeySource;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.service.EncryptedPersistenceService;
import org.adorsys.encobject.service.ExtendedStoreConnection;
import org.adorsys.encobject.service.JWEncryptionService;
import org.adorsys.encobject.service.SimplePayloadImpl;
import org.adorsys.encobject.types.KeyID;
import org.adorsys.encobject.types.OverwriteFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample use of the encobject api to implement our protocol.
 *
 * @author fpo
 */
public class DocumentPersistenceServiceImpl implements DocumentPersistenceService {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentPersistenceServiceImpl.class);

    private EncryptedPersistenceService encryptedPersistenceUtil;
    private DocumentGuardService documentGuardService;
    private ContainerPersistence containerPersistence;
    private BucketServiceImpl bucketService;

    public DocumentPersistenceServiceImpl(ExtendedStoreConnection extendedStoreConnection) {
        this.containerPersistence = new ContainerPersistence(extendedStoreConnection);
        this.encryptedPersistenceUtil = new EncryptedPersistenceService(extendedStoreConnection, new JWEncryptionService());
        this.documentGuardService = new DocumentGuardServiceImpl(extendedStoreConnection);
        this.bucketService = new BucketServiceImpl(extendedStoreConnection);
    }

    /**
     * Verschlüsselt den DocumentContent mit dem (symmetrischen) DocumentKey. Erzeugt ein Document,
     * dass den verschlüsselten DocumentContent enthält. Im Header dieses Documents steht die DocumentKeyID.
     * Das Document liegt in einem Bucket mit dem Namen documentBucketPath.
     */
    @Override
    public void persistDocument(
            DocumentKeyIDWithKey documentKeyIDWithKey,
            DocumentBucketPath documentBucketPath,
            DocumentContent documentContent,
            OverwriteFlag overwriteFlag,
            StorageMetadata storageMetadata) {

        try {
            LOGGER.info("start persist " + documentBucketPath);
            if (overwriteFlag.equals(OverwriteFlag.FALSE)) {
                if (bucketService.fileExists(documentBucketPath)) {
                    throw new FileExistsException(documentBucketPath + " existiert und overwrite flag ist false");
                }
            }
            KeySource keySource = new DocumentKeyIDWithKeyBasedSourceImpl(documentKeyIDWithKey);
            LOGGER.debug("Document wird verschlüsselt mit " + documentKeyIDWithKey);
            KeyID keyID = new KeyID(documentKeyIDWithKey.getDocumentKeyID().getValue());
            Payload payload = new SimplePayloadImpl(storageMetadata, documentContent.getValue());
            encryptedPersistenceUtil.encryptAndPersist(documentBucketPath, payload, keySource, keyID);
            LOGGER.info("finished persist " + documentBucketPath);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    /**
     *
     */
    @Override
    public Payload loadDocument(
            KeyStoreAccess keyStoreAccess,
            DocumentBucketPath documentBucketPath) {

        try {
            LOGGER.info("start load " + documentBucketPath + " " + keyStoreAccess);
            KeySource keySource = new DocumentGuardBasedKeySourceImpl(documentGuardService, keyStoreAccess);
            Payload payload = encryptedPersistenceUtil.loadAndDecrypt(documentBucketPath, keySource);
            LOGGER.info("finished load " + documentBucketPath);
            return payload;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
