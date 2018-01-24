package org.adorsys.documentsafe.layer03business.impl;

import org.adorsys.documentsafe.layer00common.exceptions.BaseException;
import org.adorsys.documentsafe.layer01persistence.types.KeyStoreID;
import org.adorsys.documentsafe.layer01persistence.types.OverwriteFlag;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.KeyStoreBucketPath;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.KeyStoreLocation;
import org.adorsys.documentsafe.layer02service.BucketService;
import org.adorsys.documentsafe.layer02service.DocumentGuardService;
import org.adorsys.documentsafe.layer02service.DocumentPersistenceService;
import org.adorsys.documentsafe.layer02service.KeyStoreService;
import org.adorsys.documentsafe.layer02service.impl.BucketServiceImpl;
import org.adorsys.documentsafe.layer02service.impl.DocumentGuardServiceImpl;
import org.adorsys.documentsafe.layer02service.impl.DocumentPersistenceServiceImpl;
import org.adorsys.documentsafe.layer02service.impl.KeyStoreServiceImpl;
import org.adorsys.documentsafe.layer02service.types.DocumentContent;
import org.adorsys.documentsafe.layer02service.types.DocumentID;
import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentBucketPath;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentContentWithContentMetaInfo;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentLocation;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAuth;
import org.adorsys.documentsafe.layer03business.utils.ContentMetaInfoUtil;
import org.adorsys.documentsafe.layer03business.exceptions.UserIDAlreadyExistsException;
import org.adorsys.documentsafe.layer03business.exceptions.UserIDDoesNotExistException;
import org.adorsys.documentsafe.layer03business.types.complex.DSDocumentMetaInfo;
import org.adorsys.documentsafe.layer03business.types.complex.DocumentFQN;
import org.adorsys.documentsafe.layer03business.types.RelativeBucketPath;
import org.adorsys.documentsafe.layer03business.types.UserHomeBucketPath;
import org.adorsys.documentsafe.layer03business.types.UserRootBucketPath;
import org.adorsys.documentsafe.layer03business.types.complex.DSDocument;
import org.adorsys.documentsafe.layer03business.types.complex.DocumentLink;
import org.adorsys.documentsafe.layer03business.types.complex.DocumentLinkAsDSDocument;
import org.adorsys.documentsafe.layer03business.types.complex.UserIDAuth;
import org.adorsys.documentsafe.layer03business.utils.GuardUtil;
import org.adorsys.documentsafe.layer03business.utils.LinkUtil;
import org.adorsys.documentsafe.layer03business.utils.UserIDUtil;
import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.service.BlobStoreContextFactory;
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
            KeyStoreBucketPath keyStoreBucketPath = UserIDUtil.getKeyStoreBucketPath(userIDAuth.getUserID());
            KeyStoreAuth keyStoreAuth = UserIDUtil.getKeyStoreAuth(userIDAuth);
            bucketService.createBucket(keyStoreBucketPath);
            KeyStoreLocation keyStoreLocation = keyStoreService.createKeyStore(keyStoreID, keyStoreAuth, keyStoreBucketPath, null);
            keyStoreAccess = new KeyStoreAccess(keyStoreLocation, keyStoreAuth);
        }
        {   // speichern einer leeren Datei, um sich den KeyStoreTypen zu merken
            UserIDUtil.saveKeyStoreTypeFile(bucketService, keyStoreAccess.getKeyStoreLocation().getKeyStoreBucketPath(), keyStoreAccess.getKeyStoreLocation().getKeyStoreType());
        }
        UserHomeBucketPath userHomeBucketPath = UserIDUtil.getHomeBucketPath(userIDAuth.getUserID());
        {   // create homeBucket
            bucketService.createBucket(userHomeBucketPath);
            createGuardForBucket(keyStoreAccess, userHomeBucketPath);
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
        DocumentLocation documentLocation = getDocumentLocation(userIDAuth, dsDocument.getDocumentFQN());
        DocumentKeyIDWithKey documentKeyIDWithKey = getOrCreateDocumentKeyIDwithKeyForBucketPath(userIDAuth, documentLocation.getDocumentBucketPath());
        DocumentLocation documentLocationResult = documentPersistenceService.persistDocument(
                documentKeyIDWithKey,
                documentLocation.getDocumentBucketPath(),
                documentLocation.getDocumentID(),
                dsDocument.getDocumentContent(),
                OverwriteFlag.FALSE,
                contentMetaInfo);
        if (!documentLocation.equals(documentLocationResult)) {
            throw new BaseException("programming error: expected " + documentLocation + " to be equal to " + documentLocationResult);
        }

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
        DocumentLocation documentLocation = getDocumentLocation(userIDAuth, documentFQN);
        KeyStoreAccess keyStoreAccess = getKeyStoreAccess(userIDAuth);
        DocumentContentWithContentMetaInfo documentContentWithContentMetaInfo = documentPersistenceService.loadDocument(keyStoreAccess, documentLocation);
        DSDocumentMetaInfo dsDocumentMetaInfo = ContentMetaInfoUtil.createDSDocumentMetaInfo(documentContentWithContentMetaInfo.getContentMetaInfo());
        if (ContentMetaInfoUtil.isLink(documentContentWithContentMetaInfo.getContentMetaInfo())) {
            LOGGER.info("start load link " + documentFQN);
            DocumentLink documentLink = LinkUtil.getDocumentLink(documentContentWithContentMetaInfo.getDocumentContent());
            DocumentLocation sourceDocumentLocation = documentLink.getSourceDocumentLocation();
            documentContentWithContentMetaInfo = documentPersistenceService.loadDocument(keyStoreAccess, sourceDocumentLocation);
            dsDocumentMetaInfo = ContentMetaInfoUtil.createDSDocumentMetaInfo(documentContentWithContentMetaInfo.getContentMetaInfo());
            LOGGER.info("finished load link " + documentFQN);
        }
        LOGGER.info("finished readDocument for " + userIDAuth + " " + documentFQN);
        return new DSDocument(documentFQN, documentContentWithContentMetaInfo.getDocumentContent(), dsDocumentMetaInfo);
    }

    @Override
    public void linkDocument(UserIDAuth userIDAuth, DocumentFQN sourceDocumentFQN, DocumentFQN destinationDocumentFQN) {
        LOGGER.info("start linkDocument for " + userIDAuth + " " + sourceDocumentFQN + " -> " + destinationDocumentFQN);

        // Wir prüfen lediglich, ob es den source Bucket gibt und ob wir darauf Zugriff haben.
        // Ob das Document selbset existiert, bleibt vorher ein Geheimnis
        DocumentLocation sourceDocumentLocation = getDocumentLocation(userIDAuth, sourceDocumentFQN);
        DocumentKeyIDWithKey sourceDocumentKeyIDWithKey = getDocumentKeyIDwithKeyForBucketPath(userIDAuth, sourceDocumentLocation.getDocumentBucketPath());

        DocumentLocation destinationDocumentLocation = getDocumentLocation(userIDAuth, destinationDocumentFQN);
        DocumentKeyIDWithKey destinationDocumentKeyIDWithKey = getOrCreateDocumentKeyIDwithKeyForBucketPath(userIDAuth, destinationDocumentLocation.getDocumentBucketPath());

        DocumentLink documentLink = new DocumentLink(sourceDocumentLocation, destinationDocumentLocation);
        DocumentLinkAsDSDocument dsDocumentLink = LinkUtil.createDSDocument(documentLink, destinationDocumentFQN);

        storeDocument(userIDAuth, dsDocumentLink);
        LOGGER.info("finished linkDocument for " + userIDAuth + " " + sourceDocumentFQN + " -> " + destinationDocumentFQN);
    }

    private KeyStoreAccess getKeyStoreAccess(UserIDAuth userIDAuth) {
        KeyStoreLocation keyStoreLocation = UserIDUtil.getKeyStoreLocation(userIDAuth.getUserID(), bucketService);
        KeyStoreAuth keyStoreAuth = UserIDUtil.getKeyStoreAuth(userIDAuth);
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStoreLocation, keyStoreAuth);
        return keyStoreAccess;
    }

    private DocumentLocation getDocumentLocation(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        UserHomeBucketPath userHomeBucketPath = UserIDUtil.getHomeBucketPath(userIDAuth.getUserID());
        RelativeBucketPath relativeBucketPath = documentFQN.getRelativeBucketPath();
        DocumentID documentID = documentFQN.getDocumentID();
        DocumentBucketPath documentBucketPath = new DocumentBucketPath(userHomeBucketPath.append(relativeBucketPath));
        DocumentLocation documentLocation = new DocumentLocation(documentID, documentBucketPath);
        return documentLocation;
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
     * @param keyStoreAccess
     * @param bucketPath
     * @return
     */
    private DocumentKeyID createGuardForBucket(KeyStoreAccess keyStoreAccess, BucketPath bucketPath) {
        LOGGER.debug("start create new guard for " + bucketPath.getObjectHandlePath());
        DocumentKeyIDWithKey documentKeyIdWithKey = documentGuardService.createDocumentKeyIdWithKey();
        documentGuardService.createSymmetricDocumentGuard(keyStoreAccess, documentKeyIdWithKey);
        GuardUtil.saveBucketGuardKeyFile(bucketService, keyStoreAccess.getKeyStoreLocation().getKeyStoreBucketPath(), bucketPath, documentKeyIdWithKey.getDocumentKeyID());
        LOGGER.debug("finished create new guard for " + bucketPath.getObjectHandlePath());
        return documentKeyIdWithKey.getDocumentKeyID();
    }

    private DocumentKeyIDWithKey getOrCreateDocumentKeyIDwithKeyForBucketPath(UserIDAuth userIDAuth, BucketPath bucketPath) {
        LOGGER.debug("search key for " + bucketPath);
        KeyStoreAccess keyStoreAccess = getKeyStoreAccess(userIDAuth);
        DocumentKeyID documentKeyID = GuardUtil.tryToLoadBucketGuardKeyFile(bucketService, keyStoreAccess.getKeyStoreLocation().getKeyStoreBucketPath(), bucketPath);
        if (documentKeyID == null) {
            documentKeyID = createGuardForBucket(keyStoreAccess, bucketPath);
        }
        DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardService.loadDocumentKeyIDWithKeyFromDocumentGuard(keyStoreAccess, documentKeyID);
        LOGGER.debug("found " + documentKeyIDWithKey + " for " + bucketPath);
        return documentKeyIDWithKey;
    }

    private DocumentKeyIDWithKey getDocumentKeyIDwithKeyForBucketPath(UserIDAuth userIDAuth, BucketPath bucketPath) {
        LOGGER.debug("get key for " + bucketPath);
        KeyStoreAccess keyStoreAccess = getKeyStoreAccess(userIDAuth);
        DocumentKeyID documentKeyID = GuardUtil.loadBucketGuardKeyFile(bucketService, keyStoreAccess.getKeyStoreLocation().getKeyStoreBucketPath(), bucketPath);
        DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardService.loadDocumentKeyIDWithKeyFromDocumentGuard(keyStoreAccess, documentKeyID);
        LOGGER.debug("found " + documentKeyIDWithKey + " for " + bucketPath);
        return documentKeyIDWithKey;
    }

}
