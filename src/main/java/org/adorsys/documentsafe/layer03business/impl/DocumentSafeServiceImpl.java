package org.adorsys.documentsafe.layer03business.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.documentsafe.layer02service.BucketService;
import org.adorsys.documentsafe.layer02service.DocumentGuardService;
import org.adorsys.documentsafe.layer02service.DocumentPersistenceService;
import org.adorsys.documentsafe.layer02service.KeyStoreService;
import org.adorsys.documentsafe.layer02service.impl.BucketServiceImpl;
import org.adorsys.documentsafe.layer02service.impl.DocumentGuardServiceImpl;
import org.adorsys.documentsafe.layer02service.impl.DocumentPersistenceServiceImpl;
import org.adorsys.documentsafe.layer02service.impl.KeyStoreServiceImpl;
import org.adorsys.documentsafe.layer02service.types.DocumentContent;
import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentBucketPath;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentContentWithContentMetaInfo;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentDirectory;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentGuardLocation;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKeyAndAccessType;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAuth;
import org.adorsys.documentsafe.layer03business.exceptions.NoWriteAccessException;
import org.adorsys.documentsafe.layer03business.exceptions.UserIDAlreadyExistsException;
import org.adorsys.documentsafe.layer03business.exceptions.UserIDDoesNotExistException;
import org.adorsys.documentsafe.layer03business.types.AccessType;
import org.adorsys.documentsafe.layer03business.types.UserHomeBucketPath;
import org.adorsys.documentsafe.layer03business.types.UserID;
import org.adorsys.documentsafe.layer03business.types.UserRootBucketPath;
import org.adorsys.documentsafe.layer03business.types.complex.DSDocument;
import org.adorsys.documentsafe.layer03business.types.complex.DSDocumentMetaInfo;
import org.adorsys.documentsafe.layer03business.types.complex.DocumentDirectoryFQN;
import org.adorsys.documentsafe.layer03business.types.complex.DocumentFQN;
import org.adorsys.documentsafe.layer03business.types.complex.DocumentLink;
import org.adorsys.documentsafe.layer03business.types.complex.DocumentLinkAsDSDocument;
import org.adorsys.documentsafe.layer03business.types.complex.UserIDAuth;
import org.adorsys.documentsafe.layer03business.utils.ContentMetaInfoUtil;
import org.adorsys.documentsafe.layer03business.utils.GrantUtil;
import org.adorsys.documentsafe.layer03business.utils.GuardUtil;
import org.adorsys.documentsafe.layer03business.utils.LinkUtil;
import org.adorsys.documentsafe.layer03business.utils.UserIDUtil;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.complextypes.KeyStoreDirectory;
import org.adorsys.encobject.complextypes.KeyStoreLocation;
import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.adorsys.encobject.types.KeyStoreID;
import org.adorsys.encobject.types.OverwriteFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 19.01.18 at 14:39.
 */
public class DocumentSafeServiceImpl implements org.adorsys.documentsafe.layer03business.DocumentSafeService {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentSafeServiceImpl.class);

    private BucketService bucketService;
    private KeyStoreService keyStoreService;
    private DocumentGuardService documentGuardService;
    private DocumentPersistenceService documentPersistenceService;

    public DocumentSafeServiceImpl(BlobStoreContextFactory factory) {
        bucketService = new BucketServiceImpl(factory);
        keyStoreService = new KeyStoreServiceImpl(factory);
        documentGuardService = new DocumentGuardServiceImpl(factory);
        documentPersistenceService = new DocumentPersistenceServiceImpl(factory);
    }

    @Override
    public void createUser(UserIDAuth userIDAuth) {
        LOGGER.info("start create user for " + userIDAuth);

        {   // check user does not exist yet
            UserRootBucketPath userRootBucketPath = UserIDUtil.getUserRootBucketPath(userIDAuth.getUserID());
            if (bucketService.bucketExists(userRootBucketPath)) {
                throw new UserIDAlreadyExistsException(userIDAuth.getUserID().toString());
            }
        }
        KeyStoreAccess keyStoreAccess = null;
        {   // create KeyStore
            KeyStoreID keyStoreID = UserIDUtil.getKeyStoreID(userIDAuth.getUserID());
            KeyStoreDirectory keyStoreDirectory = UserIDUtil.getKeyStoreDirectory(userIDAuth.getUserID());
            KeyStoreAuth keyStoreAuth = UserIDUtil.getKeyStoreAuth(userIDAuth);
            bucketService.createBucket(keyStoreDirectory);
            KeyStoreLocation keyStoreLocation = keyStoreService.createKeyStore(keyStoreID, keyStoreAuth, keyStoreDirectory, null);
            keyStoreAccess = new KeyStoreAccess(keyStoreLocation, keyStoreAuth);
        }
        {   // speichern einer leeren Datei, um sich den KeyStoreTypen zu merken
            UserIDUtil.saveKeyStoreTypeFile(bucketService, keyStoreAccess.getKeyStoreLocation().getKeyStoreDirectory(), keyStoreAccess.getKeyStoreLocation().getKeyStoreType());
        }
        UserHomeBucketPath userHomeBucketPath = UserIDUtil.getHomeBucketPath(userIDAuth.getUserID());
        {   // create homeBucket
            bucketService.createBucket(userHomeBucketPath);
            createGuardForBucket(keyStoreAccess, new DocumentDirectory(userHomeBucketPath), AccessType.WRITE);
        }
        {   // Now create a welcome file in the Home directory
            storeDocument(userIDAuth, createWelcomeDocument());
        }

        LOGGER.info("finished create user for " + userIDAuth);
    }

    @Override
    public void storeDocument(UserIDAuth userIDAuth, DSDocument dsDocument) {
        LOGGER.info("start storeDocument for " + userIDAuth + " " + dsDocument.getDocumentFQN());

        ContentMetaInfo contentMetaInfo = ContentMetaInfoUtil.createContentMetaInfo(dsDocument);
        DocumentBucketPath documentBucketPath = getTheDocumentBucketPath(userIDAuth.getUserID(), dsDocument.getDocumentFQN());
        DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = getOrCreateDocumentKeyIDwithKeyForBucketPath(userIDAuth, documentBucketPath.getDocumentDirectory(), AccessType.WRITE);
        // Hier ist keine Prüfung des Schreibrechts notwendig
        documentPersistenceService.persistDocument(
                documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey(),
                documentBucketPath,
                dsDocument.getDocumentContent(),
                OverwriteFlag.TRUE,
                contentMetaInfo);
        LOGGER.info("finished storeDocument for " + userIDAuth + " " + dsDocument.getDocumentFQN());
    }


    @Override
    public void destroyUser(UserIDAuth userIDAuth) {
        LOGGER.info("start destroy user for " + userIDAuth);
        BucketPath userRootBucket = UserIDUtil.getUserRootBucketPath(userIDAuth.getUserID());
        {   // check user does not exist yet
            if (!bucketService.bucketExists(userRootBucket)) {
                throw new UserIDDoesNotExistException(userIDAuth.getUserID().toString());
            }
        }
        {   // TODO check password is fine

        }
        bucketService.destroyBucket(userRootBucket);
        LOGGER.info("finished destroy user for " + userIDAuth);
    }

    @Override
    public DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.info("start readDocument for " + userIDAuth + " " + documentFQN);
        DocumentBucketPath documentBucketPath = getTheDocumentBucketPath(userIDAuth.getUserID(), documentFQN);
        KeyStoreAccess keyStoreAccess = getKeyStoreAccess(userIDAuth);
        DocumentContentWithContentMetaInfo documentContentWithContentMetaInfo = documentPersistenceService.loadDocument(keyStoreAccess, documentBucketPath);
        DSDocumentMetaInfo dsDocumentMetaInfo = ContentMetaInfoUtil.createDSDocumentMetaInfo(documentContentWithContentMetaInfo.getContentMetaInfo());
        if (ContentMetaInfoUtil.isLink(documentContentWithContentMetaInfo.getContentMetaInfo())) {
            LOGGER.info("start load link " + documentFQN);
            DocumentLink documentLink = LinkUtil.getDocumentLink(documentContentWithContentMetaInfo.getDocumentContent());
            DocumentBucketPath sourceDocumentBucketPath = documentLink.getSourceDocumentBucketPath();
            documentContentWithContentMetaInfo = documentPersistenceService.loadDocument(keyStoreAccess, sourceDocumentBucketPath);
            dsDocumentMetaInfo = ContentMetaInfoUtil.createDSDocumentMetaInfo(documentContentWithContentMetaInfo.getContentMetaInfo());
            LOGGER.info("finished load link " + documentFQN);
        }
        LOGGER.info("finished readDocument for " + userIDAuth + " " + documentFQN);
        return new DSDocument(documentFQN, documentContentWithContentMetaInfo.getDocumentContent(), dsDocumentMetaInfo);
    }

    @Override
    public DSDocument readDocument(UserIDAuth userIDAuth, UserID documentOwner, DocumentFQN documentFQN) {
        LOGGER.info("start readDocument for " + userIDAuth + " " + documentOwner + " " + documentFQN);
        DocumentBucketPath documentBucketPath = getTheDocumentBucketPath(documentOwner, documentFQN);
        KeyStoreAccess keyStoreAccess = getKeyStoreAccess(userIDAuth);
        DocumentContentWithContentMetaInfo documentContentWithContentMetaInfo = documentPersistenceService.loadDocument(
                keyStoreAccess, documentBucketPath);
        DSDocumentMetaInfo dsDocumentMetaInfo = ContentMetaInfoUtil.createDSDocumentMetaInfo(documentContentWithContentMetaInfo.getContentMetaInfo());
        if (ContentMetaInfoUtil.isLink(documentContentWithContentMetaInfo.getContentMetaInfo())) {
            LOGGER.info("start load link " + documentFQN);
            DocumentLink documentLink = LinkUtil.getDocumentLink(documentContentWithContentMetaInfo.getDocumentContent());
            DocumentBucketPath sourceDocumentBucketPath = documentLink.getSourceDocumentBucketPath();
            documentContentWithContentMetaInfo = documentPersistenceService.loadDocument(keyStoreAccess, sourceDocumentBucketPath);
            dsDocumentMetaInfo = ContentMetaInfoUtil.createDSDocumentMetaInfo(documentContentWithContentMetaInfo.getContentMetaInfo());
            LOGGER.info("finished load link " + documentFQN);
        }
        LOGGER.info("finisherd readDocument for " + userIDAuth + " " + documentOwner + " " + documentFQN);
        return new DSDocument(documentFQN, documentContentWithContentMetaInfo.getDocumentContent(), dsDocumentMetaInfo);
    }

    @Override
    public void linkDocument(UserIDAuth userIDAuth, DocumentFQN sourceDocumentFQN, DocumentFQN destinationDocumentFQN) {
        LOGGER.info("start linkDocument for " + userIDAuth + " " + sourceDocumentFQN + " -> " + destinationDocumentFQN);

        // Wir prüfen lediglich, ob es den source Bucket gibt und ob wir darauf Zugriff haben.
        // Ob das Document selbset existiert, bleibt vorher ein Geheimnis
        DocumentBucketPath sourceDocumentBucketPath = getTheDocumentBucketPath(userIDAuth.getUserID(), sourceDocumentFQN);
        DocumentKeyIDWithKeyAndAccessType sourceDocumentKeyIDWithKeyAndAccessType = getDocumentKeyIDwithKeyForBucketPath(userIDAuth, sourceDocumentBucketPath.getDocumentDirectory());

        DocumentBucketPath destinationDocumentBucketPath = getTheDocumentBucketPath(userIDAuth.getUserID(), destinationDocumentFQN);
        DocumentKeyIDWithKeyAndAccessType destinationDocumentKeyIDWithKeyAndAccessType = getOrCreateDocumentKeyIDwithKeyForBucketPath(userIDAuth, destinationDocumentBucketPath.getDocumentDirectory(), AccessType.WRITE);

        // TODO, die keys der destination müssen noch in das linkDocument (das faktisch ein guard ist)
        DocumentLink documentLink = new DocumentLink(sourceDocumentBucketPath, destinationDocumentBucketPath);
        DocumentLinkAsDSDocument dsDocumentLink = LinkUtil.createDSDocument(documentLink, destinationDocumentFQN);

        storeDocument(userIDAuth, dsDocumentLink);
        LOGGER.info("finished linkDocument for " + userIDAuth + " " + sourceDocumentFQN + " -> " + destinationDocumentFQN);
    }

    @Override
    public void grantAccessToUserForFolder(UserIDAuth userIDAuth, UserID receiverUserID,
                                           DocumentDirectoryFQN documentDirectoryFQN,
                                           AccessType accessType) {
        LOGGER.info("start grant access for " + userIDAuth + " to  " + receiverUserID + " for " + documentDirectoryFQN + " with " + accessType);

        {
            UserRootBucketPath userRootBucketPath = UserIDUtil.getUserRootBucketPath(userIDAuth.getUserID());
            if (!bucketService.bucketExists(userRootBucketPath)) {
                throw new UserIDDoesNotExistException(userIDAuth.getUserID().toString());
            }
        }
        {
            UserRootBucketPath userRootBucketPath = UserIDUtil.getUserRootBucketPath(receiverUserID);
            if (!bucketService.bucketExists(userRootBucketPath)) {
                throw new UserIDDoesNotExistException(receiverUserID.toString());
            }
        }

        UserHomeBucketPath homeBucketPath = UserIDUtil.getHomeBucketPath(userIDAuth.getUserID());
        DocumentDirectory documentDirectory = new DocumentDirectory(homeBucketPath.append(new BucketPath(documentDirectoryFQN.getValue())));


        AccessType grantedAccess = GrantUtil.getAccessTypeOfBucketGrantFile(bucketService, documentDirectory, userIDAuth.getUserID(), receiverUserID);
        if (grantedAccess.equals(accessType)) {
            LOGGER.debug("nothing to do. granted access already exists for " + userIDAuth + " to  " + receiverUserID + " for " + documentDirectoryFQN + " with " + accessType);
            return;
        }
        if (!grantedAccess.equals(AccessType.NONE)) {
            LOGGER.debug("granted access for " + userIDAuth + " to  " + receiverUserID + " for " + documentDirectoryFQN + " will be changed from " + grantedAccess + " to " + accessType);
        }

        DocumentKeyIDWithKeyAndAccessType usersDocumentKeyIDWithKeyAndAccessType = getOrCreateDocumentKeyIDwithKeyForBucketPath(userIDAuth, documentDirectory, AccessType.WRITE);
        {
            DocumentKeyIDWithKeyAndAccessType receiversDocumentKeyWithIDAndAccessType = new DocumentKeyIDWithKeyAndAccessType(usersDocumentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey(), accessType);
            UserIDAuth receiverUserIDAuth = new UserIDAuth(receiverUserID, null);
            KeyStoreAccess receiverKeyStoreAccess = getKeyStoreAccess(receiverUserIDAuth);
            if (AccessType.NONE.equals(accessType)) {
                deleteGuardForBucket(receiverKeyStoreAccess, receiversDocumentKeyWithIDAndAccessType, documentDirectory);
            } else {
                createAsymmetricGuardForBucket(receiverKeyStoreAccess, receiversDocumentKeyWithIDAndAccessType, documentDirectory, OverwriteFlag.TRUE);
            }
        }

        {
            // create a grant file, so we know, who received a grant for what
            GrantUtil.saveBucketGrantFile(bucketService, documentDirectory, userIDAuth.getUserID(), receiverUserID, accessType);
        }

        LOGGER.info("finished grant access for " + userIDAuth + " to  " + receiverUserID + " for " + documentDirectoryFQN + " with " + accessType);
    }


    @Override
    public void storeDocument(UserIDAuth userIDAuth, UserID documentOwner, DSDocument dsDocument) {
        LOGGER.info("start storeDocument for " + userIDAuth + " " + documentOwner + " " + dsDocument.getDocumentFQN());

        ContentMetaInfo contentMetaInfo = ContentMetaInfoUtil.createContentMetaInfo(dsDocument);
        DocumentBucketPath documentBucketPath = getTheDocumentBucketPath(documentOwner, dsDocument.getDocumentFQN());
        DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = getDocumentKeyIDwithKeyForBucketPath(userIDAuth, documentBucketPath.getDocumentDirectory());
        if (!documentKeyIDWithKeyAndAccessType.getAccessType().equals(AccessType.WRITE)) {
            throw new NoWriteAccessException(userIDAuth.getUserID(), documentOwner, dsDocument.getDocumentFQN());
        }
        documentPersistenceService.persistDocument(
                documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey(),
                documentBucketPath,
                dsDocument.getDocumentContent(),
                OverwriteFlag.TRUE,
                contentMetaInfo);
        LOGGER.info("finished storeDocument for " + userIDAuth + " " + documentOwner + " " + dsDocument.getDocumentFQN());
    }

    private DocumentKeyID createAsymmetricGuardForBucket(KeyStoreAccess keyStoreAccess,
                                                         DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType,
                                                         DocumentDirectory documentDirectory,
                                                         OverwriteFlag overwriteFlag) {
        LOGGER.debug("start create asymmetric guard for " + documentDirectory + " " + keyStoreAccess.getKeyStoreLocation().getKeyStoreDirectory());
        documentGuardService.createAsymmetricDocumentGuard(keyStoreAccess, documentKeyIDWithKeyAndAccessType, overwriteFlag);
        GuardUtil.saveBucketGuardKeyFile(bucketService, keyStoreAccess.getKeyStoreLocation().getKeyStoreDirectory(), documentDirectory, documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKeyID());
        LOGGER.debug("finished create asymmetric guard for " + documentDirectory + " " + keyStoreAccess.getKeyStoreLocation().getKeyStoreDirectory());
        return documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKeyID();
    }

    private void deleteGuardForBucket(KeyStoreAccess keyStoreAccess,
                                      DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType,
                                      DocumentDirectory documentDirectory
    ) {
        LOGGER.debug("start delete guard for " + documentDirectory + " " + keyStoreAccess.getKeyStoreLocation().getKeyStoreDirectory());
        BucketPath documentGuardFileBucketPath = DocumentGuardLocation.getBucketPathOfGuard(keyStoreAccess.getKeyStoreLocation(),
                documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKeyID());
        bucketService.deletePlainFile(documentGuardFileBucketPath);

        GuardUtil.deleteBucketGuardKeyFile(bucketService, keyStoreAccess.getKeyStoreLocation().getKeyStoreDirectory(), documentDirectory);
        LOGGER.debug("finished delete for " + documentDirectory + " " + keyStoreAccess.getKeyStoreLocation().getKeyStoreDirectory());
    }

    private KeyStoreAccess getKeyStoreAccess(UserIDAuth userIDAuth) {
        KeyStoreLocation keyStoreLocation = UserIDUtil.getKeyStoreLocation(userIDAuth.getUserID(), bucketService);
        KeyStoreAuth keyStoreAuth = UserIDUtil.getKeyStoreAuth(userIDAuth);
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStoreLocation, keyStoreAuth);
        return keyStoreAccess;
    }

    private DocumentBucketPath getTheDocumentBucketPath(UserID userID, DocumentFQN documentFQN) {
        return new DocumentBucketPath(UserIDUtil.getHomeBucketPath(userID).append(documentFQN.getValue()));
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
    private DocumentKeyID createGuardForBucket(KeyStoreAccess keyStoreAccess, DocumentDirectory documentDirectory, AccessType accessType) {
        LOGGER.debug("start create new guard for " + documentDirectory);
        DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = new DocumentKeyIDWithKeyAndAccessType(documentGuardService.createDocumentKeyIdWithKey(), accessType);
        documentGuardService.createSymmetricDocumentGuard(keyStoreAccess, documentKeyIDWithKeyAndAccessType);
        GuardUtil.saveBucketGuardKeyFile(bucketService,
                keyStoreAccess.getKeyStoreLocation().getKeyStoreDirectory(),
                documentDirectory, documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKeyID());
        LOGGER.debug("finished create new guard for " + documentDirectory);
        return documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKeyID();
    }

    private DocumentKeyIDWithKeyAndAccessType getOrCreateDocumentKeyIDwithKeyForBucketPath(UserIDAuth userIDAuth,
                                                                                           DocumentDirectory documentDirectory,
                                                                                           AccessType accessType) {
        LOGGER.debug("search key for " + documentDirectory);
        KeyStoreAccess keyStoreAccess = getKeyStoreAccess(userIDAuth);
        DocumentKeyID documentKeyID = GuardUtil.tryToLoadBucketGuardKeyFile(bucketService, keyStoreAccess.getKeyStoreLocation().getKeyStoreDirectory(), documentDirectory);
        if (documentKeyID == null) {
            documentKeyID = createGuardForBucket(keyStoreAccess, documentDirectory, accessType);
        }
        DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = documentGuardService.loadDocumentKeyIDWithKeyAndAccessTypeFromDocumentGuard(keyStoreAccess, documentKeyID);
        LOGGER.debug("found " + documentKeyIDWithKeyAndAccessType + " for " + documentDirectory);
        return documentKeyIDWithKeyAndAccessType;
    }

    private DocumentKeyIDWithKeyAndAccessType getDocumentKeyIDwithKeyForBucketPath(UserIDAuth userIDAuth, DocumentDirectory documentDirectory) {
        LOGGER.debug("get key for " + documentDirectory);
        KeyStoreAccess keyStoreAccess = getKeyStoreAccess(userIDAuth);
        DocumentKeyID documentKeyID = GuardUtil.loadBucketGuardKeyFile(bucketService, keyStoreAccess.getKeyStoreLocation().getKeyStoreDirectory(), documentDirectory);
        DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = documentGuardService.loadDocumentKeyIDWithKeyAndAccessTypeFromDocumentGuard(keyStoreAccess, documentKeyID);
        LOGGER.debug("found " + documentKeyIDWithKeyAndAccessType + " for " + documentDirectory);
        return documentKeyIDWithKeyAndAccessType;
    }


}
