package org.adorsys.docusafe.business.impl;

import com.nimbusds.jose.jwk.JWK;
import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.exceptions.NoWriteAccessException;
import org.adorsys.docusafe.business.exceptions.UserIDAlreadyExistsException;
import org.adorsys.docusafe.business.exceptions.UserIDDoesNotExistException;
import org.adorsys.docusafe.business.exceptions.WrongPasswordException;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DSDocumentStream;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.business.utils.BucketPath2FQNHelper;
import org.adorsys.docusafe.business.utils.GrantUtil;
import org.adorsys.docusafe.business.utils.GuardUtil;
import org.adorsys.docusafe.business.utils.UserIDUtil;
import org.adorsys.docusafe.service.BucketService;
import org.adorsys.docusafe.service.DocumentGuardService;
import org.adorsys.docusafe.service.DocumentPersistenceService;
import org.adorsys.docusafe.service.KeySourceService;
import org.adorsys.docusafe.service.impl.BucketServiceImpl;
import org.adorsys.docusafe.service.impl.DocumentGuardServiceImpl;
import org.adorsys.docusafe.service.impl.DocumentPersistenceServiceImpl;
import org.adorsys.docusafe.service.impl.GuardKeyType;
import org.adorsys.docusafe.service.impl.KeySourceServiceImpl;
import org.adorsys.docusafe.service.types.AccessType;
import org.adorsys.docusafe.service.types.BucketContent;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.docusafe.service.types.DocumentKeyID;
import org.adorsys.docusafe.service.types.complextypes.DocumentBucketPath;
import org.adorsys.docusafe.service.types.complextypes.DocumentGuardLocation;
import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKeyAndAccessType;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.KeyStoreAccess;
import org.adorsys.encobject.domain.KeyStoreAuth;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.domain.UserMetaData;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.api.KeyStoreService;
import org.adorsys.encobject.service.impl.KeyStoreServiceImpl;
import org.adorsys.encobject.service.impl.SimplePayloadImpl;
import org.adorsys.encobject.service.impl.SimplePayloadStreamImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.adorsys.encobject.types.OverwriteFlag;
import org.adorsys.jkeygen.keystore.KeyStoreType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.UnrecoverableEntryException;

/**
 * Created by peter on 19.01.18 at 14:39.
 */
public class DocumentSafeServiceImpl implements DocumentSafeService {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentSafeServiceImpl.class);

    private BucketService bucketService;
    private KeyStoreService keyStoreService;
    private DocumentGuardService documentGuardService;
    private DocumentPersistenceService documentPersistenceService;
    private KeySourceService keySourceService;

    public DocumentSafeServiceImpl(ExtendedStoreConnection extendedStoreConnection) {
        bucketService = new BucketServiceImpl(extendedStoreConnection);
        keyStoreService = new KeyStoreServiceImpl(extendedStoreConnection);
        documentGuardService = new DocumentGuardServiceImpl(extendedStoreConnection);
        documentPersistenceService = new DocumentPersistenceServiceImpl(extendedStoreConnection);
        keySourceService = new KeySourceServiceImpl(extendedStoreConnection);
    }

    /**
     * USER
     * ===========================================================================================
     */

    @Override
    public void createUser(UserIDAuth userIDAuth) {
        LOGGER.debug("start create user for " + userIDAuth);

        {
            if (userExists(userIDAuth.getUserID())) {
                throw new UserIDAlreadyExistsException(userIDAuth.getUserID().toString());
            }
        }
        KeyStoreAccess keyStoreAccess = null;
        {   // create KeyStore
            BucketDirectory keyStoreDirectory = UserIDUtil.getKeyStoreDirectory(userIDAuth.getUserID());
            KeyStoreAuth keyStoreAuth = UserIDUtil.getKeyStoreAuth(userIDAuth);
            bucketService.createBucket(keyStoreDirectory);
            BucketPath keyStorePath = UserIDUtil.getKeyStorePath(userIDAuth.getUserID());
            keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, keyStorePath, null);
            keyStoreAccess = new KeyStoreAccess(keyStorePath, keyStoreAuth);
        }
        BucketDirectory userHomeBucketDirectory = UserIDUtil.getHomeBucketDirectory(userIDAuth.getUserID());
        {   // create homeBucket
            bucketService.createBucket(userHomeBucketDirectory);
            createGuardForBucket(keyStoreAccess, userHomeBucketDirectory, AccessType.WRITE);
        }
        {   // Now create a welcome file in the Home directory
            storeDocument(userIDAuth, createWelcomeDocument());
        }

        LOGGER.debug("finished create user for " + userIDAuth);
    }

    @Override
    public void destroyUser(UserIDAuth userIDAuth) {
        LOGGER.debug("start destroy user for " + userIDAuth);
        BucketDirectory userRootBucketDirectory = UserIDUtil.getUserRootBucketDirectory(userIDAuth.getUserID());
        {   // check user does not exist yet
            if (!bucketService.bucketExists(userRootBucketDirectory)) {
                throw new UserIDDoesNotExistException(userIDAuth.getUserID().toString());
            }
        }
        {
            checkUserKeyPassword(userIDAuth);
        }
        bucketService.destroyBucket(userRootBucketDirectory);
        LOGGER.debug("finished destroy user for " + userIDAuth);
    }

    @Override
    public boolean userExists(UserID userID) {
        BucketDirectory userRootBucketDirectory = UserIDUtil.getUserRootBucketDirectory(userID);
        return bucketService.bucketExists(userRootBucketDirectory);
    }

    /**
     * DOCUMENT
     * ===========================================================================================
     */

    /**
     * -- byte orientiert --
     */
    @Override
    public void storeDocument(UserIDAuth userIDAuth, DSDocument dsDocument) {
        LOGGER.debug("start storeDocument for " + userIDAuth + " " + dsDocument.getDocumentFQN());

        SimpleStorageMetadataImpl storageMetadata = new SimpleStorageMetadataImpl();
        storageMetadata.mergeUserMetadata(dsDocument.getDsDocumentMetaInfo());
        storageMetadata.setSize(new Long(dsDocument.getDocumentContent().getValue().length));
        DocumentBucketPath documentBucketPath = getTheDocumentBucketPath(userIDAuth.getUserID(), dsDocument.getDocumentFQN());
        DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = getOrCreateDocumentKeyIDwithKeyForBucketPath(userIDAuth, documentBucketPath.getBucketDirectory(), AccessType.WRITE);
        // Hier ist keine Prüfung des Schreibrechts notwendig
        documentPersistenceService.persistDocument(
                documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey(),
                documentBucketPath,
                OverwriteFlag.TRUE,
                new SimplePayloadImpl(storageMetadata, dsDocument.getDocumentContent().getValue()));
        LOGGER.debug("finished storeDocument for " + userIDAuth + " " + dsDocument.getDocumentFQN());
    }

    @Override
    public DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.debug("start readDocument for " + userIDAuth + " " + documentFQN);
        DocumentBucketPath documentBucketPath = getTheDocumentBucketPath(userIDAuth.getUserID(), documentFQN);
        KeyStoreAccess keyStoreAccess = getKeyStoreAccess(userIDAuth);
        Payload payload = documentPersistenceService.loadDocument(keyStoreAccess, documentBucketPath);
        UserMetaData userMetaData = payload.getStorageMetadata().getUserMetadata();
        LOGGER.debug("finished readDocument for " + userIDAuth + " " + documentFQN);
        return new DSDocument(documentFQN, new DocumentContent(payload.getData()), new DSDocumentMetaInfo(payload.getStorageMetadata().getUserMetadata()));
    }

    /**
     * -- stream orientiert --
     */
    @Override
    public void storeDocumentStream(UserIDAuth userIDAuth, DSDocumentStream dsDocumentStream) {
        LOGGER.debug("start storeDocumentStream for " + userIDAuth + " " + dsDocumentStream.getDocumentFQN());

        SimpleStorageMetadataImpl storageMetadata = new SimpleStorageMetadataImpl();
        storageMetadata.mergeUserMetadata(dsDocumentStream.getDsDocumentMetaInfo());
        DocumentBucketPath documentBucketPath = getTheDocumentBucketPath(userIDAuth.getUserID(), dsDocumentStream.getDocumentFQN());
        DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = getOrCreateDocumentKeyIDwithKeyForBucketPath(userIDAuth, documentBucketPath.getBucketDirectory(), AccessType.WRITE);
        // Hier ist keine Prüfung des Schreibrechts notwendig
        documentPersistenceService.persistDocumentStream(
                documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey(),
                documentBucketPath,
                OverwriteFlag.TRUE,
                new SimplePayloadStreamImpl(storageMetadata, dsDocumentStream.getDocumentStream()));
        LOGGER.debug("finished storeDocument for " + userIDAuth + " " + dsDocumentStream.getDocumentFQN());
    }


    @Override
    public DSDocumentStream readDocumentStream(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        try {
            LOGGER.debug("start readDocumentStream for " + userIDAuth + " " + documentFQN);
            DocumentBucketPath documentBucketPath = getTheDocumentBucketPath(userIDAuth.getUserID(), documentFQN);
            KeyStoreAccess keyStoreAccess = getKeyStoreAccess(userIDAuth);
            PayloadStream payloadStream = documentPersistenceService.loadDocumentStream(keyStoreAccess, documentBucketPath);
            UserMetaData userMetaData = payloadStream.getStorageMetadata().getUserMetadata();
            LOGGER.debug("finished readDocumentStream for " + userIDAuth + " " + documentFQN);
            return new DSDocumentStream(documentFQN, payloadStream.openStream(), new DSDocumentMetaInfo(payloadStream.getStorageMetadata().getUserMetadata()));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public void deleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        checkUserKeyPassword(userIDAuth);
        DocumentBucketPath documentBucketPath = getTheDocumentBucketPath(userIDAuth.getUserID(), documentFQN);
        bucketService.deletePlainFile(documentBucketPath);
    }

    @Override
    public boolean documentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        checkUserKeyPassword(userIDAuth);
        DocumentBucketPath documentBucketPath = getTheDocumentBucketPath(userIDAuth.getUserID(), documentFQN);
        return bucketService.fileExists(documentBucketPath);
    }

    @Override
    public void deleteFolder(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN) {
        BucketDirectory homeBucketDirectory = UserIDUtil.getHomeBucketDirectory(userIDAuth.getUserID());
        BucketDirectory documentBucketDirectory = homeBucketDirectory.append(new BucketDirectory(documentDirectoryFQN.getValue()));
        bucketService.deletePlainFolder(documentBucketDirectory);
    }

    @Override
    public BucketContentFQN list(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        LOGGER.debug("list directroy " + documentDirectoryFQN + " for " + userIDAuth.getUserID());
        checkUserKeyPassword(userIDAuth);
        BucketDirectory homeBucketDirectory = UserIDUtil.getHomeBucketDirectory(userIDAuth.getUserID());
        BucketDirectory bucketDirectory = documentDirectoryFQN.prepend(homeBucketDirectory);
        BucketContentFQNImpl ret = new BucketContentFQNImpl();
        BucketContent bucketContent = bucketService.readDocumentBucket(bucketDirectory, recursiveFlag);
        bucketContent.getFiles().forEach(bucketPath ->
                ret.getFiles().add(BucketPath2FQNHelper.path2FQN(homeBucketDirectory, bucketPath)));

        // Filtere das eigene directroy raus.
        DocumentDirectoryFQN dir = documentDirectoryFQN.getValue().startsWith(BucketPath.BUCKET_SEPARATOR) ?
                documentDirectoryFQN :
                new DocumentDirectoryFQN(BucketPath.BUCKET_SEPARATOR + documentDirectoryFQN.getValue());
        bucketContent.getSubdirectories().forEach(subdirectory -> {
            DocumentDirectoryFQN dirFQN = BucketPath2FQNHelper.directory2FQN(homeBucketDirectory, subdirectory);
            if (!dirFQN.equals(dir)) {
                ret.getDirectories().add(dirFQN);
            }
        });

        return ret;
    }

    /**
     * GRANT/DOCUMENT
     * ===========================================================================================
     */
    @Override
    public void grantAccessToUserForFolder(UserIDAuth userIDAuth, UserID receiverUserID,
                                           DocumentDirectoryFQN documentDirectoryFQN,
                                           AccessType accessType) {
        LOGGER.debug("start grant access for " + userIDAuth + " to  " + receiverUserID + " for " + documentDirectoryFQN + " with " + accessType);

        {
            BucketDirectory userRootBucketDirectory = UserIDUtil.getUserRootBucketDirectory(userIDAuth.getUserID());
            if (!bucketService.bucketExists(userRootBucketDirectory)) {
                throw new UserIDDoesNotExistException(userIDAuth.getUserID().toString());
            }
        }
        {
            BucketDirectory userRootBucketDirectory = UserIDUtil.getUserRootBucketDirectory(receiverUserID);
            if (!bucketService.bucketExists(userRootBucketDirectory)) {
                throw new UserIDDoesNotExistException(receiverUserID.toString());
            }
        }

        BucketDirectory homeBucketDirectory = UserIDUtil.getHomeBucketDirectory(userIDAuth.getUserID());
        BucketDirectory documentBucketDirectory = homeBucketDirectory.append(new BucketDirectory(documentDirectoryFQN.getValue()));

        AccessType grantedAccess = GrantUtil.getAccessTypeOfBucketGrantFile(bucketService, documentBucketDirectory, userIDAuth.getUserID(), receiverUserID);
        if (grantedAccess.equals(accessType)) {
            LOGGER.debug("nothing to do. granted access already exists for " + userIDAuth + " to  " + receiverUserID + " for " + documentDirectoryFQN + " with " + accessType);
            return;
        }
        if (!grantedAccess.equals(AccessType.NONE)) {
            LOGGER.debug("granted access for " + userIDAuth + " to  " + receiverUserID + " for " + documentDirectoryFQN + " will be changed from " + grantedAccess + " to " + accessType);
        }

        DocumentKeyIDWithKeyAndAccessType usersDocumentKeyIDWithKeyAndAccessType = getOrCreateDocumentKeyIDwithKeyForBucketPath(userIDAuth, documentBucketDirectory, AccessType.WRITE);
        {
            DocumentKeyIDWithKeyAndAccessType receiversDocumentKeyWithIDAndAccessType = new DocumentKeyIDWithKeyAndAccessType(usersDocumentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey(), accessType);
            UserIDAuth receiverUserIDAuth = new UserIDAuth(receiverUserID, null);
            KeyStoreAccess receiverKeyStoreAccess = getKeyStoreAccess(receiverUserIDAuth);
            if (AccessType.NONE.equals(accessType)) {
                deleteGuardForBucket(receiverKeyStoreAccess, receiversDocumentKeyWithIDAndAccessType, documentBucketDirectory);
            } else {
                createAsymmetricGuardForBucket(receiverKeyStoreAccess, receiversDocumentKeyWithIDAndAccessType, documentBucketDirectory, OverwriteFlag.TRUE);
            }
        }

        {
            // create a grant file, so we know, who received a grant for what
            GrantUtil.saveBucketGrantFile(bucketService, documentBucketDirectory, userIDAuth.getUserID(), receiverUserID, accessType);
        }

        LOGGER.debug("finished grant access for " + userIDAuth + " to  " + receiverUserID + " for " + documentDirectoryFQN + " with " + accessType);
    }

    @Override
    public void storeGrantedDocument(UserIDAuth userIDAuth, UserID documentOwner, DSDocument dsDocument) {
        LOGGER.debug("start storeDocument for " + userIDAuth + " " + documentOwner + " " + dsDocument.getDocumentFQN());

        SimpleStorageMetadataImpl storageMetadata = new SimpleStorageMetadataImpl();
        storageMetadata.setSize(new Long(dsDocument.getDocumentContent().getValue().length));
        DocumentBucketPath documentBucketPath = getTheDocumentBucketPath(documentOwner, dsDocument.getDocumentFQN());
        DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = getDocumentKeyIDwithKeyForBucketPath(userIDAuth, documentBucketPath.getBucketDirectory());
        if (!documentKeyIDWithKeyAndAccessType.getAccessType().equals(AccessType.WRITE)) {
            throw new NoWriteAccessException(userIDAuth.getUserID(), documentOwner, dsDocument.getDocumentFQN());
        }
        documentPersistenceService.persistDocument(
                documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey(),
                documentBucketPath,
                OverwriteFlag.TRUE,
                new SimplePayloadImpl(storageMetadata, dsDocument.getDocumentContent().getValue()));
        LOGGER.debug("finished storeDocument for " + userIDAuth + " " + documentOwner + " " + dsDocument.getDocumentFQN());
    }


    @Override
    public DSDocument readGrantedDocument(UserIDAuth userIDAuth, UserID documentOwner, DocumentFQN documentFQN) {
        LOGGER.debug("start readDocument for " + userIDAuth + " " + documentOwner + " " + documentFQN);
        DocumentBucketPath documentBucketPath = getTheDocumentBucketPath(documentOwner, documentFQN);
        KeyStoreAccess keyStoreAccess = getKeyStoreAccess(userIDAuth);
        Payload payload = documentPersistenceService.loadDocument(keyStoreAccess, documentBucketPath);
        UserMetaData userMetaData = payload.getStorageMetadata().getUserMetadata();
        LOGGER.debug("finisherd readDocument for " + userIDAuth + " " + documentOwner + " " + documentFQN);
        return new DSDocument(documentFQN, new DocumentContent(payload.getData()), new DSDocumentMetaInfo(payload.getStorageMetadata().getUserMetadata()));
    }

    @Override
    public JWK findPublicEncryptionKey(UserID userID) {
        KeyStoreAccess keyStoreAccess = getKeyStoreAccess(new UserIDAuth(userID, null));
        return keySourceService.findPublicEncryptionKey(keyStoreAccess);
    }

    private DocumentKeyID createAsymmetricGuardForBucket(KeyStoreAccess keyStoreAccess,
                                                         DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType,
                                                         BucketDirectory documentDirectory,
                                                         OverwriteFlag overwriteFlag) {
        LOGGER.debug("start create asymmetric guard for " + documentDirectory + " " + keyStoreAccess.getKeyStorePath().getBucketDirectory());
        documentGuardService.createDocumentGuardFor(GuardKeyType.PUBLIC_KEY, keyStoreAccess, documentKeyIDWithKeyAndAccessType, overwriteFlag);
        GuardUtil.saveBucketGuardKeyFile(bucketService, keyStoreAccess.getKeyStorePath().getBucketDirectory(), documentDirectory, documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKeyID());
        LOGGER.debug("finished create asymmetric guard for " + documentDirectory + " " + keyStoreAccess.getKeyStorePath().getBucketDirectory());
        return documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKeyID();
    }

    private void deleteGuardForBucket(KeyStoreAccess keyStoreAccess,
                                      DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType,
                                      BucketDirectory documentDirectory
    ) {
        LOGGER.debug("start delete guard for " + documentDirectory);
        BucketPath documentGuardFileBucketPath = DocumentGuardLocation.getBucketPathOfGuard(keyStoreAccess.getKeyStorePath(),
                documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKeyID());
        bucketService.deletePlainFile(documentGuardFileBucketPath);

        GuardUtil.deleteBucketGuardKeyFile(bucketService, keyStoreAccess.getKeyStorePath().getBucketDirectory(), documentDirectory);
        LOGGER.debug("finished delete guard for " + documentDirectory);
    }

    private KeyStoreAccess getKeyStoreAccess(UserIDAuth userIDAuth) {
        BucketPath keyStorePath = UserIDUtil.getKeyStorePath(userIDAuth.getUserID());
        KeyStoreAuth keyStoreAuth = UserIDUtil.getKeyStoreAuth(userIDAuth);
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStorePath, keyStoreAuth);
        return keyStoreAccess;
    }

    private DocumentBucketPath getTheDocumentBucketPath(UserID userID, DocumentFQN documentFQN) {
        return new DocumentBucketPath(UserIDUtil.getHomeBucketDirectory(userID).appendName(documentFQN.getValue()));
    }

    private DSDocument createWelcomeDocument() {
        String text = "Welcome to the DocumentStore";
        DocumentContent documentContent = new DocumentContent(text.getBytes());
        DocumentFQN documentFQN = new DocumentFQN("README.txt");
        DSDocument dsDocument = new DSDocument(documentFQN, documentContent, null);
        return dsDocument;
    }


    /**
     * Es wird extra nur die KeyID zurückgegeben. Damit der Zugriff auf den Key wirklich über den
     * KeyStore erfolgt und damit dann auch getestet ist.
     */
    private DocumentKeyID createGuardForBucket(KeyStoreAccess keyStoreAccess, BucketDirectory documentDirectory, AccessType accessType) {
        LOGGER.debug("start create new guard for " + documentDirectory);
        DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = new DocumentKeyIDWithKeyAndAccessType(documentGuardService.createDocumentKeyIdWithKey(), accessType);
        documentGuardService.createDocumentGuardFor(GuardKeyType.SECRET_KEY, keyStoreAccess, documentKeyIDWithKeyAndAccessType, OverwriteFlag.FALSE);
        GuardUtil.saveBucketGuardKeyFile(bucketService,
                keyStoreAccess.getKeyStorePath().getBucketDirectory(),
                documentDirectory, documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKeyID());
        LOGGER.debug("finished create new guard for " + documentDirectory);
        return documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKeyID();
    }

    private DocumentKeyIDWithKeyAndAccessType getOrCreateDocumentKeyIDwithKeyForBucketPath(UserIDAuth userIDAuth,
                                                                                           BucketDirectory documentDirectory,
                                                                                           AccessType accessType) {
        LOGGER.debug("search key for " + documentDirectory);
        KeyStoreAccess keyStoreAccess = getKeyStoreAccess(userIDAuth);
        DocumentKeyID documentKeyID = GuardUtil.tryToLoadBucketGuardKeyFile(bucketService, keyStoreAccess.getKeyStorePath().getBucketDirectory(), documentDirectory);
        if (documentKeyID == null) {
            documentKeyID = createGuardForBucket(keyStoreAccess, documentDirectory, accessType);
        }
        DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = documentGuardService.loadDocumentKeyIDWithKeyAndAccessTypeFromDocumentGuard(keyStoreAccess, documentKeyID);
        LOGGER.debug("found " + documentKeyIDWithKeyAndAccessType + " for " + documentDirectory);
        return documentKeyIDWithKeyAndAccessType;
    }

    private DocumentKeyIDWithKeyAndAccessType getDocumentKeyIDwithKeyForBucketPath(UserIDAuth userIDAuth, BucketDirectory documentDirectory) {
        LOGGER.debug("get key for " + documentDirectory);
        KeyStoreAccess keyStoreAccess = getKeyStoreAccess(userIDAuth);
        DocumentKeyID documentKeyID = GuardUtil.loadBucketGuardKeyFile(bucketService, keyStoreAccess.getKeyStorePath().getBucketDirectory(), documentDirectory);
        DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = documentGuardService.loadDocumentKeyIDWithKeyAndAccessTypeFromDocumentGuard(keyStoreAccess, documentKeyID);
        LOGGER.debug("found " + documentKeyIDWithKeyAndAccessType + " for " + documentDirectory);
        return documentKeyIDWithKeyAndAccessType;
    }

    private void checkUserKeyPassword(UserIDAuth userIDAuth) {
        KeyStoreAccess keyStoreAccess = getKeyStoreAccess(userIDAuth);
        BucketDirectory documentDirectory = UserIDUtil.getHomeBucketDirectory(userIDAuth.getUserID());
        DocumentKeyID documentKeyID = GuardUtil.loadBucketGuardKeyFile(bucketService, keyStoreAccess.getKeyStorePath().getBucketDirectory(), documentDirectory);
        try {
            documentGuardService.loadDocumentKeyIDWithKeyAndAccessTypeFromDocumentGuard(keyStoreAccess, documentKeyID);
        } catch (BaseException e) {
            if (e.getCause() instanceof UnrecoverableEntryException) {
                throw new WrongPasswordException(userIDAuth.getUserID());
            }
        }
    }


}
