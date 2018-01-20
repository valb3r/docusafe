package org.adorsys.documentsafe.layer03business.impl;

import org.adorsys.documentsafe.layer01persistence.types.KeyStoreID;
import org.adorsys.documentsafe.layer01persistence.types.ListRecursiveFlag;
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
import org.adorsys.documentsafe.layer02service.types.PlainFileContent;
import org.adorsys.documentsafe.layer02service.types.PlainFileName;
import org.adorsys.documentsafe.layer02service.types.complextypes.BucketContent;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentBucketPath;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentLocation;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAuth;
import org.adorsys.documentsafe.layer03business.exceptions.UserIDAlreadyExistsException;
import org.adorsys.documentsafe.layer03business.exceptions.UserIDDoesNotExistException;
import org.adorsys.documentsafe.layer03business.types.DocumentFQN;
import org.adorsys.documentsafe.layer03business.types.RelativeBucketPath;
import org.adorsys.documentsafe.layer03business.types.UserRootBucketPath;
import org.adorsys.documentsafe.layer03business.types.UserHomeBucketPath;
import org.adorsys.documentsafe.layer03business.utils.GuardUtil;
import org.adorsys.documentsafe.layer03business.utils.UserIDUtil;
import org.adorsys.documentsafe.layer03business.types.complex.UserIDAuth;
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
            LOGGER.info("for " + userIDAuth + " a new KeyStore with " + keyStoreAccess + " has been created at " + keyStoreLocation);
        }
        {   // speichern einer leeren Datei, um sich den KeyStoreTypen zu merken
            UserIDUtil.safeKeyStoreType(userIDAuth.getUserID(), keyStoreAccess.getKeyStoreLocation().getKeyStoreType(), bucketService);
        }
        UserHomeBucketPath userHomeBucketPath = UserIDUtil.getHomeBucketPath(userIDAuth.getUserID());
        {   // create homeBucket
            bucketService.createBucket(userHomeBucketPath);
        }
        {   // create document Guard for homeBucket
            DocumentKeyIDWithKey documentKeyIdWithKeyForHomeBucketPath = documentGuardService.createDocumentKeyIdWithKey();
            documentGuardService.createSymmetricDocumentGuard(keyStoreAccess, documentKeyIdWithKeyForHomeBucketPath);
            // Erzeugen einer leeren Datei, die die Zuordnung zwischen Guard und Bucket macht
            PlainFileName plainFileName = GuardUtil.getHelperFilenameForGuardAndBucket(documentKeyIdWithKeyForHomeBucketPath.getDocumentKeyID(), userHomeBucketPath);
            bucketService.createPlainFile(keyStoreAccess.getKeyStoreLocation().getKeyStoreBucketPath(),
                    plainFileName, new PlainFileContent("not encrypted".getBytes()));
        }
        {   // Now create a welcome file in the Home directory
            // Retrieve DocumentKey first. Do not use the one, created before, but read
            // it from the PlainFile Info
            BucketContent bucketContent = bucketService.readDocumentBucket(keyStoreAccess.getKeyStoreLocation().getKeyStoreBucketPath(), ListRecursiveFlag.FALSE);
            DocumentKeyID documentKeyID = GuardUtil.getDocumentKeyID(bucketContent, userHomeBucketPath);
            DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardService.loadDocumentKeyIDWithKeyFromDocumentGuard(keyStoreAccess, documentKeyID);
            LOGGER.debug("USERS HOME DIR documentKeyWithID is " + documentKeyIDWithKey);

            DocumentContent documentContent = new DocumentContent("Welcome to the documentsafe.".getBytes());
            DocumentID documentID = new DocumentID("README.txt");
            DocumentBucketPath documentBucketPath = new DocumentBucketPath(userHomeBucketPath.getObjectHandlePath());
            documentPersistenceService.persistDocument(documentKeyIDWithKey, documentBucketPath, documentID, documentContent, OverwriteFlag.FALSE);
        }

    }

    @Override
    public void destroyUser(UserIDAuth userIDAuth) {
        BucketPath userRootBucket = UserIDUtil.getUserRootBucketPath(userIDAuth.getUserID());
        {   // check user does not exist yet
            if (!bucketService.bucketExists(userRootBucket)) {
                throw new UserIDDoesNotExistException(userIDAuth.getUserID().toString());
            }
        }
        {   // check password is fine
        }
        bucketService.destroyBucket(userRootBucket);
    }

    @Override
    public DocumentContent readDocument(UserIDAuth userIDAuth, DocumentFQN documentPath) {
        DocumentLocation documentLocation;
        {
            UserHomeBucketPath userHomeBucketPath = UserIDUtil.getHomeBucketPath(userIDAuth.getUserID());
            RelativeBucketPath relativeBucketPath = documentPath.getRelativeBucketPath();
            DocumentID documentID = documentPath.getDocumentID();
            DocumentBucketPath documentBucketPath = new DocumentBucketPath(userHomeBucketPath.append(relativeBucketPath));
            documentLocation = new DocumentLocation(documentID, documentBucketPath);
        }
        KeyStoreAccess keyStoreAccess;
        {
            KeyStoreLocation keyStoreLocation = UserIDUtil.getKeyStoreLocation(userIDAuth.getUserID(), bucketService);
            KeyStoreAuth keyStoreAuth = UserIDUtil.getKeyStoreAuth(userIDAuth);
            keyStoreAccess = new KeyStoreAccess(keyStoreLocation, keyStoreAuth);
        }
        LOGGER.info("for " + userIDAuth + " with " + keyStoreAccess + " the KeyStore will be loaded from " + keyStoreAccess.getKeyStoreLocation() );

        return documentPersistenceService.loadDocument(keyStoreAccess, documentLocation);
    }
}
