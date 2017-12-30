package org.adorsys.resource.server.service;

import java.io.IOException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.security.auth.callback.CallbackHandler;

import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.params.EncryptionParams;
import org.adorsys.encobject.service.ContainerExistsException;
import org.adorsys.encobject.service.MissingKeyAlgorithmException;
import org.adorsys.encobject.service.MissingKeystoreAlgorithmException;
import org.adorsys.encobject.service.MissingKeystoreProviderException;
import org.adorsys.encobject.service.ObjectNotFoundException;
import org.adorsys.encobject.service.UnknownContainerException;
import org.adorsys.encobject.service.WrongKeyCredentialException;
import org.adorsys.encobject.service.WrongKeystoreCredentialException;
import org.adorsys.jjwk.selector.UnsupportedEncAlgorithmException;
import org.adorsys.jjwk.selector.UnsupportedKeyLengthException;
import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;
import org.adorsys.resource.server.basetypes.BucketName;
import org.adorsys.resource.server.basetypes.DocumentContent;
import org.adorsys.resource.server.basetypes.DocumentGuardName;
import org.adorsys.resource.server.basetypes.DocumentID;
import org.adorsys.resource.server.basetypes.UserID;
import org.adorsys.resource.server.complextypes.DocumentGuard;
import org.adorsys.resource.server.persistence.ExtendedObjectPersistence;
import org.adorsys.resource.server.persistence.PersistentObjectWrapper;
import org.adorsys.resource.server.utils.KeystoreAdapter;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * Sample use of the encobject api to implement our protocol.
 *
 * @author fpo
 */
public class DocumentPersistenceService {
	
    private ExtendedObjectPersistence objectPersistence;
    private DocumentGuardService documentGuardService;
    
    public DocumentPersistenceService(ExtendedObjectPersistence objectPersistence,
			DocumentGuardService documentGuardService) {
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
     * @throws ContainerExistsException
     * @throws WrongKeyCredentialException
     * @throws UnknownContainerException
     * @throws IOException
     * @throws MissingKeyAlgorithmException
     * @throws MissingKeystoreProviderException
     * @throws MissingKeystoreAlgorithmException
     * @throws WrongKeystoreCredentialException
     * @throws ObjectNotFoundException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedKeyLengthException
     * @throws UnsupportedEncAlgorithmException
     */
    public void persistDocument(UserID userId,
                                CallbackHandler userKeystoreHandler,
                                CallbackHandler keyPassHandler,
                                BucketName bucketName,
                                DocumentGuardName documentGuardName,
                                DocumentID documentID,
                                DocumentContent documentContent)
            throws ContainerExistsException, CertificateException, ObjectNotFoundException, WrongKeystoreCredentialException, MissingKeystoreAlgorithmException, MissingKeystoreProviderException, MissingKeyAlgorithmException, IOException, UnknownContainerException, WrongKeyCredentialException, NoSuchAlgorithmException, UnsupportedEncAlgorithmException, UnsupportedKeyLengthException {

        // Load DokumentKeyID from guard.
        DocumentGuard documentGuard = documentGuardService.loadDocumentGuard(userId, bucketName, documentGuardName, userKeystoreHandler, keyPassHandler);

        // Create object handle
        ObjectHandle docHandle = new ObjectHandle(bucketName.getValue(), documentID.getValue());

        // Store object.
        ContentMetaInfo metaIno = null;
        EncryptionParams encParams = null;
        PersistentObjectWrapper wrapper = new PersistentObjectWrapper(documentContent.getValue(), metaIno, documentGuardName.getDocumnentKeyID().getValue(), docHandle);

        
        // Put SecretKey into a keystore so we can use existing ObjectPersistence API
        CallbackHandler docKeyStoreHandler = new PasswordCallbackHandler(RandomStringUtils.randomAlphanumeric(16).toCharArray());
        KeyStore docKeyStore = KeystoreAdapter.wrapSecretKey2KeyStore(documentGuard.getDocumentKey().getSecretKey(), documentGuardName.getDocumnentKeyID().getValue(), keyPassHandler);

        objectPersistence.storeObject(wrapper, docKeyStore, docKeyStoreHandler, encParams);
    }

}
