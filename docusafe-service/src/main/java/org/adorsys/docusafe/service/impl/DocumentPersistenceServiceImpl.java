package org.adorsys.docusafe.service.impl;

import org.adorsys.docusafe.service.DocumentGuardService;
import org.adorsys.docusafe.service.DocumentPersistenceService;
import org.adorsys.docusafe.service.keysource.DocumentGuardBasedKeySourceImpl;
import org.adorsys.docusafe.service.keysource.DocumentKeyIDWithKeyBasedSourceImpl;
import org.adorsys.docusafe.service.types.complextypes.DocumentBucketPath;
import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.encobject.domain.KeyStoreAccess;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.exceptions.FileExistsException;
import org.adorsys.encobject.service.api.ContainerPersistence;
import org.adorsys.encobject.service.api.EncryptedPersistenceService;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.api.KeySource;
import org.adorsys.encobject.service.impl.AESEncryptionStreamServiceImpl;
import org.adorsys.encobject.service.impl.ContainerPersistenceImpl;
import org.adorsys.encobject.service.impl.EncryptedPersistenceServiceImpl;
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

    private EncryptedPersistenceService encryptedPersistenceService;
    private DocumentGuardService documentGuardService;
    private ContainerPersistence containerPersistence;
    private BucketServiceImpl bucketService;

    public DocumentPersistenceServiceImpl(ExtendedStoreConnection extendedStoreConnection) {
        this.containerPersistence = new ContainerPersistenceImpl(extendedStoreConnection);
        if (System.getProperty("UGLY_NO_ENCRYPTION") != null) {
            LOGGER.info("ACHTUNG, NO ENCRYPTION");
            this.encryptedPersistenceService = new EncryptedPersistenceServiceImpl(extendedStoreConnection, new NoEncryptionStreamServiceImpl());
        } else {
            this.encryptedPersistenceService = new EncryptedPersistenceServiceImpl(extendedStoreConnection, new AESEncryptionStreamServiceImpl());
        }
        this.documentGuardService = new DocumentGuardServiceImpl(extendedStoreConnection);
        this.bucketService = new BucketServiceImpl(extendedStoreConnection);
    }

    /**
     * Verschlüsselt den DocumentContent mit dem (symmetrischen) DocumentKey. Erzeugt ein Document,
     * das den verschlüsselten DocumentContent enthält. Im Header dieses Documents steht die DocumentKeyID.
     * Das Document liegt in einem Bucket mit dem Namen documentBucketPath.
     */
    @Override
    public void persistDocument(
            DocumentKeyIDWithKey documentKeyIDWithKey,
            DocumentBucketPath documentBucketPath,
            OverwriteFlag overwriteFlag,
            Payload payload) {

        LOGGER.debug("start persist " + documentBucketPath);
        if (overwriteFlag.equals(OverwriteFlag.FALSE)) {
            if (bucketService.fileExists(documentBucketPath)) {
                throw new FileExistsException(documentBucketPath + " existiert und overwrite flag ist false");
            }
        }
        KeySource keySource = new DocumentKeyIDWithKeyBasedSourceImpl(documentKeyIDWithKey);
        LOGGER.debug("Document wird verschlüsselt mit " + documentKeyIDWithKey);
        KeyID keyID = new KeyID(documentKeyIDWithKey.getDocumentKeyID().getValue());
        encryptedPersistenceService.encryptAndPersist(documentBucketPath, payload, keySource, keyID);
        LOGGER.debug("finished persist " + documentBucketPath);
    }

    @Override
    public Payload loadDocument(
            KeyStoreAccess keyStoreAccess,
            DocumentBucketPath documentBucketPath) {

        LOGGER.debug("start load " + documentBucketPath + " " + keyStoreAccess);
        KeySource keySource = new DocumentGuardBasedKeySourceImpl(documentGuardService, keyStoreAccess);
        Payload payload = encryptedPersistenceService.loadAndDecrypt(documentBucketPath, keySource);
        LOGGER.debug("finished load " + documentBucketPath);
        return payload;
    }

    @Override
    public void persistDocumentStream(
            DocumentKeyIDWithKey documentKeyIDWithKey,
            DocumentBucketPath documentBucketPath,
            OverwriteFlag overwriteFlag,
            PayloadStream payloadStream) {

        LOGGER.debug("start persist stream " + documentBucketPath);
        if (overwriteFlag.equals(OverwriteFlag.FALSE)) {
            if (bucketService.fileExists(documentBucketPath)) {
                throw new FileExistsException(documentBucketPath + " existiert und overwrite flag ist false");
            }
        }
        KeySource keySource = new DocumentKeyIDWithKeyBasedSourceImpl(documentKeyIDWithKey);
        LOGGER.debug("Document wird verschlüsselt mit " + documentKeyIDWithKey);
        KeyID keyID = new KeyID(documentKeyIDWithKey.getDocumentKeyID().getValue());
        encryptedPersistenceService.encryptAndPersistStream(documentBucketPath, payloadStream, keySource, keyID);
        LOGGER.debug("finished persist " + documentBucketPath);
    }

    @Override
    public PayloadStream loadDocumentStream(KeyStoreAccess keyStoreAccess, DocumentBucketPath documentBucketPath) {
        LOGGER.debug("start load stream " + documentBucketPath + " " + keyStoreAccess);
        KeySource keySource = new DocumentGuardBasedKeySourceImpl(documentGuardService, keyStoreAccess);
        PayloadStream payloadStream = encryptedPersistenceService.loadAndDecryptStream(documentBucketPath, keySource);
        LOGGER.debug("finished load " + documentBucketPath);
        return payloadStream;
    }
}
