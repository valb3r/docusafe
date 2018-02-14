package org.adorsys.documentsafe.layer02service.impl;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer02service.DocumentGuardService;
import org.adorsys.documentsafe.layer02service.DocumentPersistenceService;
import org.adorsys.documentsafe.layer02service.keysource.DocumentGuardBasedKeySourceImpl;
import org.adorsys.documentsafe.layer02service.keysource.DocumentKeyIDWithKeyBasedSourceImpl;
import org.adorsys.documentsafe.layer02service.types.DocumentContent;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentBucketPath;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentContentWithContentMetaInfo;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.keysource.KeySource;
import org.adorsys.encobject.params.EncryptionParams;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.service.ExtendedStoreConnection;
import org.adorsys.encobject.service.JWEPersistence;
import org.adorsys.encobject.service.PersistentObjectWrapper;
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

    private JWEPersistence jwePersistence;
    private DocumentGuardService documentGuardService;
    private ContainerPersistence containerPersistence;

    public DocumentPersistenceServiceImpl(ExtendedStoreConnection extendedStoreConnection) {
        this.containerPersistence = new ContainerPersistence(extendedStoreConnection);
        this.jwePersistence = new JWEPersistence(extendedStoreConnection);
        this.documentGuardService = new DocumentGuardServiceImpl(extendedStoreConnection);
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
            ContentMetaInfo contentMetaInfo) {

        try {
            LOGGER.info("start persist " + documentBucketPath);

            // Create object handle
            ObjectHandle location = documentBucketPath.getObjectHandle();

            // Store object.
            EncryptionParams encParams = null;

            KeySource keySource = new DocumentKeyIDWithKeyBasedSourceImpl(documentKeyIDWithKey);
            LOGGER.debug("Document wird verschlüsselt mit " + documentKeyIDWithKey);
            // Create container if non existent
            if (location.getContainer() != null) {
                if (!containerPersistence.containerExists(location.getContainer())) {
                    containerPersistence.createContainer(location.getContainer());
                }
            }
            KeyID keyID = new KeyID(documentKeyIDWithKey.getDocumentKeyID().getValue());
            jwePersistence.storeObject(documentContent.getValue(), contentMetaInfo, location, keySource, keyID, encParams, overwriteFlag);
            LOGGER.info("finished persist " + documentBucketPath);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    /**
     *
     */
    @Override
    public DocumentContentWithContentMetaInfo loadDocument(
            KeyStoreAccess keyStoreAccess,
            DocumentBucketPath documentBucketPath) {

        try {
            LOGGER.info("start load " + documentBucketPath + " " + keyStoreAccess);
            KeySource keySource = new DocumentGuardBasedKeySourceImpl(documentGuardService, keyStoreAccess);
            PersistentObjectWrapper persistentObjectWrapper = jwePersistence.loadObject(documentBucketPath.getObjectHandle(), keySource);
            DocumentContent documentContent = new DocumentContent(persistentObjectWrapper.getData());
            ContentMetaInfo contentMetaInfo = persistentObjectWrapper.getMetaIno();
            DocumentContentWithContentMetaInfo documentContentWithContentMetaInfo = new DocumentContentWithContentMetaInfo(documentContent, contentMetaInfo);
            LOGGER.info("finished load " + documentBucketPath);
            return documentContentWithContentMetaInfo;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
