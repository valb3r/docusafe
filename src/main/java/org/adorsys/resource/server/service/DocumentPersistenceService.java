package org.adorsys.resource.server.service;

import javax.security.auth.callback.CallbackHandler;

import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.params.EncryptionParams;
import org.adorsys.resource.server.basetypes.DocumentContent;
import org.adorsys.resource.server.basetypes.DocumentGuardName;
import org.adorsys.resource.server.basetypes.DocumentID;
import org.adorsys.resource.server.basetypes.UserID;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.DocumentGuardBasedKeySourceImpl;
import org.adorsys.resource.server.persistence.ExtendedObjectPersistence;
import org.adorsys.resource.server.persistence.KeySource;
import org.adorsys.resource.server.persistence.PersistentObjectWrapper;
import org.adorsys.resource.server.persistence.basetypes.BucketName;
import org.adorsys.resource.server.persistence.basetypes.KeyID;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreName;

/**
 * Sample use of the encobject api to implement our protocol.
 *
 * @author fpo
 */
public class DocumentPersistenceService {
	
    private ExtendedObjectPersistence objectPersistence;
    private DocumentGuardService documentGuardService;
    
    public DocumentPersistenceService(ExtendedObjectPersistence objectPersistence,DocumentGuardService documentGuardService) {
		super();
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
    public void persistDocument(
    							CallbackHandler userKeystoreHandler,
                                CallbackHandler keyPassHandler,
                                DocumentGuardName documentGuardName,
                                BucketName documentBucketName,
                                DocumentID documentID,
                                DocumentContent documentContent) {
    	
    	try {

	        // Create object handle
	        ObjectHandle location = new ObjectHandle(documentBucketName.getValue(), documentID.getValue());
	
	        // Store object.
	        ContentMetaInfo metaInfo = null;
	        EncryptionParams encParams = null;

	        KeyID keyID = new KeyID(documentGuardName.getDocumentKeyID().getValue());
			KeySource keySource = new DocumentGuardBasedKeySourceImpl(documentGuardService, documentGuardName.getKeyStoreName(), userKeystoreHandler, keyPassHandler);
			objectPersistence.storeObject(documentContent.getValue(), metaInfo, location, keySource, keyID , encParams);
    	} catch (Exception e){
    		BaseExceptionHandler.handle(e);
    	}
    }
    
    /**
     * 
     * @param userID
     * @param userKeystoreHandler
     * @param keyPassHandler
     * @param documentID
     * @return
     */
    public PersistentObjectWrapper loadDocument(
    		KeyStoreName keyStoreName,
			CallbackHandler userKeystoreHandler,
            CallbackHandler keyPassHandler,
			BucketName documentBucketName,
			DocumentID documentID){
    	
    	try {
	        
	        KeySource keySource = new DocumentGuardBasedKeySourceImpl(documentGuardService, keyStoreName, userKeystoreHandler, keyPassHandler);

	        // Create object handle
	        ObjectHandle location = new ObjectHandle(documentBucketName.getValue(), documentID.getValue());
			return objectPersistence.loadObject(location, keySource);
    	} catch (Exception e){
    		throw BaseExceptionHandler.handle(e);
    	}
    }

}
