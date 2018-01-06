package org.adorsys.resource.server.service;

import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.params.EncryptionParams;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.resource.server.basetypes.DocumentContent;
import org.adorsys.resource.server.basetypes.DocumentID;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.DocumentGuardBasedKeySourceImpl;
import org.adorsys.resource.server.persistence.ExtendedObjectPersistence;
import org.adorsys.resource.server.persistence.KeySource;
import org.adorsys.resource.server.persistence.PersistentObjectWrapper;
import org.adorsys.resource.server.persistence.basetypes.DocumentBucketName;
import org.adorsys.resource.server.persistence.basetypes.KeyID;
import org.adorsys.resource.server.persistence.complextypes.DocumentGuardLocation;
import org.adorsys.resource.server.persistence.complextypes.DocumentLocation;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreAuth;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreLocation;

/**
 * Sample use of the encobject api to implement our protocol.
 *
 * @author fpo
 */
public class DocumentPersistenceService {
	
    private ExtendedObjectPersistence objectPersistence;
    private DocumentGuardService documentGuardService;
	private ContainerPersistence containerPersistence;

    public DocumentPersistenceService(ContainerPersistence containerPersistence, ExtendedObjectPersistence objectPersistence,DocumentGuardService documentGuardService) {
		super();
		this.containerPersistence = containerPersistence;
		this.objectPersistence = objectPersistence;
		this.documentGuardService = documentGuardService;
	}



	/**
     * Stored the document on behalf of a user.
     * - We use userid to find the user key store
     * - the store pass is a standard value derived from the user id
     * - the key pass is extracted from the bearer token authenticating the user.
     * - The bucket name specifies the user container.
     * - The document guard name specifies the key used by a user to encrypt the document. This must have been created in a former call.
     * - The document id is created by the user. If not unique, existing document will be overriden
     * - DOcument bytes found in document content.
     *
     */
    public DocumentLocation persistDocument(
    							KeyStoreAuth keyStoreAuth,
                                DocumentGuardLocation documentGuardLocation,
                                DocumentBucketName documentBucketName,
                                DocumentID documentID,
                                DocumentContent documentContent) {
    	
    	try {

	        // Create object handle
	        ObjectHandle location = new ObjectHandle(documentBucketName.getValue(), documentID.getValue());
	
	        // Store object.
	        ContentMetaInfo metaInfo = null;
	        EncryptionParams encParams = null;

	        KeyID keyID = new KeyID(documentGuardLocation.getDocumentKeyID().getValue());
			KeySource keySource = new DocumentGuardBasedKeySourceImpl(documentGuardService, documentGuardLocation.getKeyStoreLocation(), keyStoreAuth);
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
    
    /**
     * 
     */
    public PersistentObjectWrapper loadDocument(
    		KeyStoreLocation keyStoreLocation,
			KeyStoreAuth keyStoreAuth,
			DocumentLocation documentLocation){
    	
    	try {
	        KeySource keySource = new DocumentGuardBasedKeySourceImpl(documentGuardService, keyStoreLocation, keyStoreAuth);
			return objectPersistence.loadObject(documentLocation.getLocationHanlde(), keySource);
    	} catch (Exception e){
    		throw BaseExceptionHandler.handle(e);
    	}
    }

}
