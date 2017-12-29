package org.adorsys.resource.server.service;

import java.io.IOException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.crypto.SecretKey;
import javax.security.auth.callback.CallbackHandler;

import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.params.EncryptionParams;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.adorsys.encobject.service.BlobStoreKeystorePersistence;
import org.adorsys.encobject.service.ContainerExistsException;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.service.KeystorePersistence;
import org.adorsys.encobject.service.MissingKeyAlgorithmException;
import org.adorsys.encobject.service.MissingKeystoreAlgorithmException;
import org.adorsys.encobject.service.MissingKeystoreProviderException;
import org.adorsys.encobject.service.ObjectNotFoundException;
import org.adorsys.encobject.service.ObjectPersistence;
import org.adorsys.encobject.service.StoreConnection;
import org.adorsys.encobject.service.UnknownContainerException;
import org.adorsys.encobject.service.WrongKeyCredentialException;
import org.adorsys.encobject.service.WrongKeystoreCredentialException;
import org.adorsys.jjwk.selector.UnsupportedEncAlgorithmException;
import org.adorsys.jjwk.selector.UnsupportedKeyLengthException;
import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;
import org.adorsys.resource.server.basetypes.*;
import org.adorsys.resource.server.utils.KeystoreAdapter;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * Sample use of the encobject api to implement our protocol.
 *
 * @author fpo
 */
public class SamplePersistenceService {

    private StoreConnection blobStoreConnection = null;
    private ObjectPersistence objectPersistence = new ObjectPersistence(blobStoreConnection);
    private ContainerPersistence containerPersistence = new ContainerPersistence(blobStoreConnection);
    private BlobStoreContextFactory blobStoreContextFactory;
    private KeystorePersistence keystorePersistence = new BlobStoreKeystorePersistence(blobStoreContextFactory);

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

        // Ensure bucket name is set.
        if (bucketName == null) throw new IllegalArgumentException("bucketName can not be null");

        // Check existence of container and return error.
        if (!containerPersistence.containerExists(bucketName.getValue()))
            throw new ContainerExistsException(bucketName.getValue());

        // Load key from guard.
        SecretKey secretKey = loadSecretKeyFromGuard(userId, bucketName, documentGuardName, userKeystoreHandler, keyPassHandler);

        // Put SecretKey into a keystore so we can use existing ObjectPersistence API
        CallbackHandler randomHandler = new PasswordCallbackHandler(RandomStringUtils.randomAlphanumeric(16).toCharArray());
        DocKeyID docKeyID = parseDocKeyID(documentGuardName);
        KeyStore docKeyStore = KeystoreAdapter.wrapSecretKEy2KeyStore(secretKey, docKeyID, keyPassHandler);

        // Create object handle
        ObjectHandle docHandle = new ObjectHandle(bucketName.getValue(), documentID.getValue());

        // Store object.
        ContentMetaInfo metaIno = null;
        EncryptionParams encParams = null;
        objectPersistence.storeObject(documentContent.getValue(), metaIno, docHandle, docKeyStore, docKeyID.getValue(), randomHandler, encParams);
    }

    /*
     * Loading the secret key from the guard.
     */
    private SecretKey loadSecretKeyFromGuard(UserID userID, BucketName bucketName, DocumentGuardName docuGuardName, CallbackHandler userKeystoreHandler,
                                             CallbackHandler userKeyPassHandler) throws ObjectNotFoundException, CertificateException, WrongKeystoreCredentialException, MissingKeystoreAlgorithmException, MissingKeystoreProviderException, MissingKeyAlgorithmException, IOException, UnknownContainerException, WrongKeyCredentialException {

        // keystore name is user id + keystore
        // TODO : we have a similar idea in the class: org.adorsys.encobject.userdata.UserDataNamingPolicy
        KeyStoreName keystoreName = userKeystoreNameFromUserId(userID);

        // load user keystore from bucket.
        ObjectHandle keystoreHandle = new ObjectHandle(bucketName.getValue(), keystoreName.getValue());
        if (!keystorePersistence.hasKeystore(keystoreHandle)) {
            throw new ObjectNotFoundException("user keystore not found.");
        }
        KeyStore userKeystore = keystorePersistence.loadKeystore(keystoreHandle, userKeystoreHandler);


        // load guard file
        ObjectHandle guardHandle = new ObjectHandle(bucketName.getValue(), docuGuardName.getValue());
        byte[] guardObject = objectPersistence.loadObject(guardHandle, userKeystore, userKeyPassHandler);

        return deserializeSecretKey(guardObject);
    }

    /*
     * Parses the DokKeyID from the docuGuardName
     */
    private DocKeyID parseDocKeyID(DocumentGuardName docuGuardName) {
        // TODO Auto-generated method stub
        return null;
    }

    private KeyStoreName userKeystoreNameFromUserId(UserID userID) {
        // keystore name is user id + keystore
        // TODO : we have a similar idea in the class: org.adorsys.encobject.userdata.UserDataNamingPolicy
        return new KeyStoreName(userID.getValue() + "_Keystore");
    }

    /*
     * Deserializes the secret key. In order not to define a proper format, we reuse the keystore format system.
     */
    private SecretKey deserializeSecretKey(byte[] guardObject)
    // TODO move this to a strategy class
            throws CertificateException, WrongKeystoreCredentialException, MissingKeystoreAlgorithmException,
            MissingKeystoreProviderException, MissingKeyAlgorithmException, IOException, WrongKeyCredentialException {
        // TODO use NullCallback instead.
        CallbackHandler nullCallbankHandler = null;
        String docKeyAlias = "docuSecretKey";
        KeyStore secretKeystore = KeystoreAdapter.fromBytes(guardObject, docKeyAlias, nullCallbankHandler);

        return (SecretKey) KeystoreAdapter.readKey(secretKeystore, docKeyAlias, nullCallbankHandler);
    }
}
