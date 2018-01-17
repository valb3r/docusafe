package org.adorsys.documentsafe.layer02service.impl;

import org.adorsys.documentsafe.layer01persistence.types.OverwriteFlag;
import org.adorsys.documentsafe.layer02service.InterfaceDocumentGuardService;
import org.adorsys.documentsafe.layer02service.InterfaceDocumentPersistenceService;
import org.adorsys.documentsafe.layer02service.types.DocumentID;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.params.EncryptionParams;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.documentsafe.layer00common.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer01persistence.ExtendedObjectPersistence;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentBucketPath;
import org.adorsys.documentsafe.layer02service.types.DocumentContent;
import org.adorsys.documentsafe.layer01persistence.types.KeyID;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentLocation;
import org.adorsys.documentsafe.layer02service.keysource.DocumentGuardBasedKeySourceImpl;
import org.adorsys.documentsafe.layer02service.keysource.DocumentKeyIDWithKeyBasedSourceImpl;
import org.adorsys.documentsafe.layer01persistence.keysource.KeySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample use of the encobject api to implement our protocol.
 *
 * @author fpo
 */
public class DocumentPersistenceService implements InterfaceDocumentPersistenceService {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentPersistenceService.class);

    private ExtendedObjectPersistence objectPersistence;
    private InterfaceDocumentGuardService documentGuardService;
    private ContainerPersistence containerPersistence;

    public DocumentPersistenceService(ContainerPersistence containerPersistence, ExtendedObjectPersistence objectPersistence, InterfaceDocumentGuardService documentGuardService) {
        super();
        this.containerPersistence = containerPersistence;
        this.objectPersistence = objectPersistence;
        this.documentGuardService = documentGuardService;
    }

    /**
     * Verschlüsselt den DocumentContent mit dem (symmetrischen) DocumentKey. Erzeugt ein Document,
     * dass den verschlüsselten DocumentContent enthält. Im Header dieses Documents steht die DocumentKeyID.
     * Das Document liegt in einem Bucket mit dem Namen documentBucketPath.
     */
    @Override
    public DocumentLocation persistDocument(
            DocumentKeyIDWithKey documentKeyIDWithKey,
            DocumentBucketPath documentBucketPath,
            DocumentID documentID,
            DocumentContent documentContent,
            OverwriteFlag overwriteFlag) {

        try {
            LOGGER.info("start persist document with " + documentID);

            // Create object handle
            ObjectHandle location = new ObjectHandle(documentBucketPath.getFirstBucket().getValue(), documentBucketPath.getSubBuckets() + documentID.getValue());

            // Store object.
            ContentMetaInfo metaInfo = null;
            EncryptionParams encParams = null;

            KeySource keySource = new DocumentKeyIDWithKeyBasedSourceImpl(documentKeyIDWithKey);
            LOGGER.debug("Document wird verschlüsselt mit " + documentKeyIDWithKey);
            // Create container if non existent
            if (!containerPersistence.containerExists(location.getContainer())) {
                containerPersistence.creteContainer(location.getContainer());
            }
            KeyID keyID = new KeyID(documentKeyIDWithKey.getDocumentKeyID().getValue());
            objectPersistence.storeObject(documentContent.getValue(), metaInfo, location, keySource, keyID, encParams, overwriteFlag);
            DocumentLocation documentLocation = new DocumentLocation(documentID, documentBucketPath);
            LOGGER.info("finished persist document with " + documentID + " @ " + documentLocation);
            return documentLocation;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    /**
     *
     */
    @Override
    public DocumentContent loadDocument(
            KeyStoreAccess keyStoreAccess,
            DocumentLocation documentLocation) {

        try {
            LOGGER.info("start load document @ " + documentLocation);
            KeySource keySource = new DocumentGuardBasedKeySourceImpl(documentGuardService, keyStoreAccess);
            DocumentContent documentContent = new DocumentContent(objectPersistence.loadObject(documentLocation.getLocationHandle(), keySource).getData());
            LOGGER.info("finished load document @ " + documentLocation);
            return documentContent;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
