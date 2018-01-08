package org.adorsys.resource.server.service;

import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.params.EncryptionParams;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.DocumentGuardBasedKeySourceImpl;
import org.adorsys.resource.server.persistence.DocumentKeyIDWithKeyBasedSourceImpl;
import org.adorsys.resource.server.persistence.ExtendedObjectPersistence;
import org.adorsys.resource.server.persistence.KeySource;
import org.adorsys.resource.server.persistence.basetypes.DocumentBucketName;
import org.adorsys.resource.server.persistence.basetypes.DocumentContent;
import org.adorsys.resource.server.persistence.basetypes.DocumentID;
import org.adorsys.resource.server.persistence.basetypes.KeyID;
import org.adorsys.resource.server.persistence.complextypes.DocumentKeyIDWithKey;
import org.adorsys.resource.server.persistence.complextypes.DocumentLocation;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreAccess;

/**
 * Sample use of the encobject api to implement our protocol.
 *
 * @author fpo
 */
public class DocumentPersistenceService {

    private ExtendedObjectPersistence objectPersistence;
    private DocumentGuardService documentGuardService;
    private ContainerPersistence containerPersistence;

    public DocumentPersistenceService(ContainerPersistence containerPersistence, ExtendedObjectPersistence objectPersistence, DocumentGuardService documentGuardService) {
        super();
        this.containerPersistence = containerPersistence;
        this.objectPersistence = objectPersistence;
        this.documentGuardService = documentGuardService;
    }

    /**
     * Verschlüsselt den DocumentContent mit dem (symmetrischen) DocumentKey. Erzeugt ein Document,
     * dass den verschlüsselten DocumentContent enthält. Im Header dieses Documents steht die DocumentKeyID.
     * Das Document liegt in einem Bucket mit dem Namen documentBucketName.
     */
    public DocumentLocation persistDocument(
            DocumentKeyIDWithKey documentKeyIDWithKey,
            DocumentBucketName documentBucketName,
            DocumentID documentID,
            DocumentContent documentContent) {

        try {

            // Create object handle
            ObjectHandle location = new ObjectHandle(documentBucketName.getValue(), documentID.getValue());

            // Store object.
            ContentMetaInfo metaInfo = null;
            EncryptionParams encParams = null;

            KeySource keySource = new DocumentKeyIDWithKeyBasedSourceImpl(documentKeyIDWithKey);
            // Create container if non existent
            if (!containerPersistence.containerExists(location.getContainer())) {
                containerPersistence.creteContainer(location.getContainer());
            }
            KeyID keyID = new KeyID(documentKeyIDWithKey.getDocumentKeyID().getValue());
            objectPersistence.storeObject(documentContent.getValue(), metaInfo, location, keySource, keyID, encParams);
            return new DocumentLocation(documentID, documentBucketName);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

/*
    public DocumentLocation persistDocument(
    							KeyStoreAccess keyStoreAccess,
                                DocumentKeyID documentKeyID,
                                DocumentBucketName documentBucketName,
                                DocumentID documentID,
                                DocumentContent documentContent) {
    	
    	try {

	        // Create object handle
	        ObjectHandle location = new ObjectHandle(documentBucketName.getValue(), documentID.getValue());
	
	        // Store object.
	        ContentMetaInfo metaInfo = null;
	        EncryptionParams encParams = null;

	        KeyID keyID = new KeyID(documentKeyID.getValue());
			KeySource keySource = new DocumentGuardBasedKeySourceImpl(documentGuardService, keyStoreAccess);
			// Create container if non existent
			if(!containerPersistence.containerExists(location.getContainer())){
				containerPersistence.creteContainer(location.getContainer());
			}
			objectPersistence.storeObject(documentContent.getValue(), metaInfo, location, keySource, keyID , encParams);
			return new DocumentLocation(documentID, documentBucketName);
    	} catch (Exception e){
    		throw BaseExceptionHandler.handle(e);
    	}
    }
*/

    /**
     *
     */
    public DocumentContent loadDocument(
            KeyStoreAccess keyStoreAccess,
            DocumentLocation documentLocation) {

        try {
            KeySource keySource = new DocumentGuardBasedKeySourceImpl(documentGuardService, keyStoreAccess);
            return new DocumentContent(objectPersistence.loadObject(documentLocation.getLocationHandle(), keySource).getData());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
