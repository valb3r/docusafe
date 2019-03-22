package org.adorsys.docusafe.business.impl;

import dagger.Lazy;
import dagger.internal.DoubleCheck;
import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.exceptions.UserIDAlreadyExistsException;
import org.adorsys.docusafe.business.exceptions.UserIDDoesNotExistException;
import org.adorsys.docusafe.business.exceptions.WrongPasswordException;
import org.adorsys.docusafe.business.impl.caches.DocumentGuardCache;
import org.adorsys.docusafe.business.impl.caches.UserAuthCache;
import org.adorsys.docusafe.business.types.MoveType;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.*;
import org.adorsys.docusafe.business.utils.BucketPath2FQNHelper;
import org.adorsys.docusafe.business.utils.UserIDUtil;
import org.adorsys.docusafe.service.BucketService;
import org.adorsys.docusafe.service.DocumentGuardService;
import org.adorsys.docusafe.service.DocumentPersistenceService;
import org.adorsys.docusafe.service.KeySourceService;
import org.adorsys.docusafe.service.impl.*;
import org.adorsys.docusafe.service.types.BucketContent;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.docusafe.service.types.DocumentKey;
import org.adorsys.docusafe.service.types.DocumentKeyID;
import org.adorsys.docusafe.service.types.complextypes.DocumentBucketPath;
import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.*;
import org.adorsys.encobject.exceptions.SymmetricEncryptionException;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.api.KeyStore2KeySourceHelper;
import org.adorsys.encobject.service.api.KeyStoreService;
import org.adorsys.encobject.service.impl.KeyStoreServiceImpl;
import org.adorsys.encobject.service.impl.SimplePayloadImpl;
import org.adorsys.encobject.service.impl.SimplePayloadStreamImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.adorsys.encobject.types.OverwriteFlag;
import org.adorsys.encobject.types.PublicKeyJWK;
import org.adorsys.encobject.types.SecretKeyIDWithKey;
import org.adorsys.jkeygen.keystore.KeyStoreType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.security.KeyStore;

/**
 * Created by peter on 19.01.18 at 14:39.
 */
public class DocumentSafeServiceImpl implements DocumentSafeService, DocumentKeyID2DocumentKeyCache {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentSafeServiceImpl.class);

    private final BucketService bucketService;
    private final KeyStoreService keyStoreService;
    private final DocumentGuardService documentGuardService;
    private final Lazy<DocumentPersistenceService> documentPersistenceService;
    private final KeySourceService keySourceService;
    private final ExtendedStoreConnection extendedStoreConnection;
    private final DocusafeCacheWrapper docusafeCacheWrapper;

    @Inject
    public DocumentSafeServiceImpl(
            BucketService bucketService,
            KeyStoreService keyStoreService,
            DocumentGuardService documentGuardService,
            Lazy<DocumentPersistenceService> documentPersistenceService,
            KeySourceService keySourceService,
            ExtendedStoreConnection extendedStoreConnection,
            DocusafeCacheWrapper docusafeCacheWrapper) {
        this.bucketService = bucketService;
        this.keyStoreService = keyStoreService;
        this.documentGuardService = documentGuardService;
        this.documentPersistenceService = documentPersistenceService;
        this.keySourceService = keySourceService;
        this.extendedStoreConnection = extendedStoreConnection;
        this.docusafeCacheWrapper = docusafeCacheWrapper;
    } 

    /**
     * @deprecated please use Dagger-based injection instead.
     */
    @Deprecated
    public DocumentSafeServiceImpl(ExtendedStoreConnection extendedStoreConnection) {
        this.extendedStoreConnection = extendedStoreConnection;
        this.bucketService = new BucketServiceImpl(extendedStoreConnection);
        this.keyStoreService = new KeyStoreServiceImpl(extendedStoreConnection);
        this.documentGuardService = new DocumentGuardServiceImpl(extendedStoreConnection);
        this.documentPersistenceService = DoubleCheck.lazy(() -> new DocumentPersistenceServiceImpl(extendedStoreConnection, this));
        this.keySourceService = new KeySourceServiceImpl(extendedStoreConnection);
        this.docusafeCacheWrapper = new DocusafeCacheWrapperImpl(CacheType.GUAVA);
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
        }
        {
            bucketService.createBucket(UserIDUtil.getInboxDirectory(userIDAuth.getUserID()));
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
                throw new UserIDDoesNotExistException(userIDAuth.getUserID());
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
        // getOrCreate dient hier nur der Authentifizierung, koennte zum Schreiben unverschluesselter Documente entfallen

        DocumentKeyIDWithKey documentKeyIDWithKey = getAnySecretKeyIDWithKeyFromKeyStore(userIDAuth);
        LOGGER.info(documentKeyIDWithKey.toString());

        if (UserMetaDataUtil.isNotEncrypted(storageMetadata.getUserMetadata())) {
            documentPersistenceService.get().persistDocument(
                    documentBucketPath,
                    OverwriteFlag.TRUE,
                    new SimplePayloadImpl(storageMetadata, dsDocument.getDocumentContent().getValue()));
            LOGGER.debug("finished storeDocument unencrypted document for " + userIDAuth + " " + dsDocument.getDocumentFQN());
            return;
        }

        documentPersistenceService.get().encryptAndPersistDocument(
                documentKeyIDWithKey,
                documentBucketPath,
                OverwriteFlag.TRUE,
                new SimplePayloadImpl(storageMetadata, dsDocument.getDocumentContent().getValue()));
        LOGGER.debug("finished storeDocument encrypted document for " + userIDAuth + " " + dsDocument.getDocumentFQN());
    }

    @Override
    public DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.debug("start readDocument for " + userIDAuth + " " + documentFQN);
        DocumentBucketPath documentBucketPath = getTheDocumentBucketPath(userIDAuth.getUserID(), documentFQN);
        StorageMetadata storageMetadata = extendedStoreConnection.getStorageMetadata(documentBucketPath);
        if (UserMetaDataUtil.isNotEncrypted(storageMetadata.getUserMetadata())) {
            checkUserKeyPassword(userIDAuth);
            Payload payload = documentPersistenceService.get().loadDocument(storageMetadata, documentBucketPath);
            DSDocument dsDocument = new DSDocument(documentFQN, new DocumentContent(payload.getData()), new DSDocumentMetaInfo(payload.getStorageMetadata().getUserMetadata()));
            LOGGER.debug("finished readDocument for " + userIDAuth + " " + documentFQN);
            return dsDocument;
        }

        KeyStoreAccess keyStoreAccess = getKeyStoreAccess(userIDAuth);
        Payload payload = documentPersistenceService.get().loadAndDecryptDocument(storageMetadata, keyStoreAccess, documentBucketPath);
        LOGGER.debug("finished readDocument for " + userIDAuth + " " + documentFQN);
        DSDocument dsDocument = new DSDocument(documentFQN, new DocumentContent(payload.getData()), new DSDocumentMetaInfo(payload.getStorageMetadata().getUserMetadata()));
        // man könnte auch früher prüfen, aber das wäre doppelt so teuer
        if (UserMetaDataUtil.isNotEncrypted(dsDocument.getDsDocumentMetaInfo())) {
            checkUserKeyPassword(userIDAuth);
        }
        return dsDocument;
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
        // getOrCreate dient hier nur der Authentifizierung, koennte zum Schreiben unverschluesselter Documente entfallen
        DocumentKeyIDWithKey myDocumentKeyIDwithKey = getAnySecretKeyIDWithKeyFromKeyStore(userIDAuth);

        if (UserMetaDataUtil.isNotEncrypted(storageMetadata.getUserMetadata())) {
            documentPersistenceService.get().persistDocumentStream(
                    documentBucketPath,
                    OverwriteFlag.TRUE,
                    new SimplePayloadStreamImpl(storageMetadata, dsDocumentStream.getDocumentStream()));
            LOGGER.debug("finished store and unencrypted document stream for " + userIDAuth + " " + dsDocumentStream.getDocumentFQN());
            return;
        }

        documentPersistenceService.get().encryptAndPersistDocumentStream(
                myDocumentKeyIDwithKey,
                documentBucketPath,
                OverwriteFlag.TRUE,
                new SimplePayloadStreamImpl(storageMetadata, dsDocumentStream.getDocumentStream()));
        LOGGER.debug("finished storeDocument encrypted document stream for " + userIDAuth + " " + dsDocumentStream.getDocumentFQN());
    }


    @Override
    public DSDocumentStream readDocumentStream(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        try {
            LOGGER.debug("start readDocumentStream for " + userIDAuth + " " + documentFQN);
            DocumentBucketPath documentBucketPath = getTheDocumentBucketPath(userIDAuth.getUserID(), documentFQN);

            StorageMetadata storageMetadata = extendedStoreConnection.getStorageMetadata(documentBucketPath);
            if (UserMetaDataUtil.isNotEncrypted(storageMetadata.getUserMetadata())) {
                checkUserKeyPassword(userIDAuth);
                PayloadStream payloadStream = documentPersistenceService.get().loadDocumentStream(storageMetadata, documentBucketPath);
                DSDocumentStream dsDocumentStream = new DSDocumentStream(documentFQN, payloadStream.openStream(), new DSDocumentMetaInfo(payloadStream.getStorageMetadata().getUserMetadata()));
                LOGGER.debug("finished readDocumentStream for " + userIDAuth + " " + documentFQN);
                return dsDocumentStream;
            }

            KeyStoreAccess keyStoreAccess = getKeyStoreAccess(userIDAuth);
            PayloadStream payloadStream = documentPersistenceService.get().loadAndDecryptDocumentStream(storageMetadata, keyStoreAccess, documentBucketPath);
            LOGGER.debug("finished read and decrypt DocumentStream for " + userIDAuth + " " + documentFQN);
            DSDocumentStream dsDocumentStream = new DSDocumentStream(documentFQN, payloadStream.openStream(), new DSDocumentMetaInfo(payloadStream.getStorageMetadata().getUserMetadata()));
            return dsDocumentStream;
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
    public BucketContentFQNWithUserMetaData list(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        LOGGER.debug("list directory " + documentDirectoryFQN + " for " + userIDAuth.getUserID());
        checkUserKeyPassword(userIDAuth);
        BucketDirectory homeBucketDirectory = UserIDUtil.getHomeBucketDirectory(userIDAuth.getUserID());
        BucketDirectory bucketDirectory = documentDirectoryFQN.prepend(homeBucketDirectory);
        BucketContentFQNWithUserMataDataImpl ret = new BucketContentFQNWithUserMataDataImpl();
        BucketContent bucketContent = bucketService.readDocumentBucket(bucketDirectory, recursiveFlag);
        bucketContent.getFiles().forEach(bucketPath -> {
                    DocumentFQN filename = BucketPath2FQNHelper.path2FQN(homeBucketDirectory, bucketPath);
                    ret.getFiles().add(filename);
                    ret.put(filename, bucketContent.getUserMetaData(bucketPath));
                }
        );

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
     * INBOX STUFF
     * ===========================================================================================
     */
    @Override
    public void moveDocumnetToInboxOfUser(UserIDAuth userIDAuth, UserID receiverUserID, DocumentFQN sourceDocumentFQN, DocumentFQN destDocumentFQN, MoveType moveType) {
        // Das ist eine Kopie von storeDocument, sollte besser gelöst werden , insbesondere wird hier im Universalfall der nicht Verschlüsselung doch verschlüsselt
        LOGGER.debug("start moveDocumentToInboxOfUser " + "FROM:" + userIDAuth.getUserID() + " " + sourceDocumentFQN + " TO:" + receiverUserID + " " + destDocumentFQN);
        DSDocument document = readDocument(userIDAuth, sourceDocumentFQN);

        writeDocumentToInboxOfUser(receiverUserID, document, destDocumentFQN);

        if (moveType.equals(MoveType.MOVE)) {
            deleteDocument(userIDAuth, sourceDocumentFQN);
        }
        LOGGER.debug("finished moveDocumentToInboxOfUser " + "FROM:" + userIDAuth.getUserID() + " " + sourceDocumentFQN + " TO:" + receiverUserID + " " + destDocumentFQN);
    }

    @Override
    public BucketContentFQNWithUserMetaData listInbox(UserIDAuth userIDAuth) {
        checkUserKeyPassword(userIDAuth);
        // Das ist eine Kopie von list, muss besser gelöst werden
        LOGGER.debug("start list of inbox for user " + userIDAuth.getUserID());
        BucketDirectory homeBucketDirectory = UserIDUtil.getInboxDirectory(userIDAuth.getUserID());
        LOGGER.debug("finished list of inbox for user " + userIDAuth.getUserID());

        BucketDirectory bucketDirectory = homeBucketDirectory;
        BucketContentFQNWithUserMataDataImpl ret = new BucketContentFQNWithUserMataDataImpl();
        BucketContent bucketContent = bucketService.readDocumentBucket(bucketDirectory, ListRecursiveFlag.TRUE);
        bucketContent.getFiles().forEach(bucketPath -> {
                    DocumentFQN filename = BucketPath2FQNHelper.path2FQN(homeBucketDirectory, bucketPath);
                    ret.getFiles().add(filename);
                    ret.put(filename, bucketContent.getUserMetaData(bucketPath));
                }
        );

        // Filtere das eigene directroy raus.
        DocumentDirectoryFQN dir = new DocumentDirectoryFQN("/");
        bucketContent.getSubdirectories().forEach(subdirectory -> {
            DocumentDirectoryFQN dirFQN = BucketPath2FQNHelper.directory2FQN(homeBucketDirectory, subdirectory);
            if (!dirFQN.equals(dir)) {
                ret.getDirectories().add(dirFQN);
            }
        });

        return ret;
    }

    @Override
    public DSDocument moveDocumentFromInbox(UserIDAuth userIDAuth, DocumentFQN source, DocumentFQN destination, OverwriteFlag overwriteFlag) {
        // Das ist eine Kopie von readDocument, sollte besser gelöst werden, insbesondere wird hier im Universalfall der nicht Verschlüsselung doch verschlüsselt
        LOGGER.debug("start readDocument from inbox for " + userIDAuth + " from " + source + " to " + destination);

        DSDocument dsDocument = readDocumentFromInbox(userIDAuth, source);
        SimpleStorageMetadataImpl storageMetadata = new SimpleStorageMetadataImpl();
        storageMetadata.mergeUserMetadata(dsDocument.getDsDocumentMetaInfo());
        storageMetadata.setSize(new Long(dsDocument.getDocumentContent().getValue().length));

        LOGGER.debug("document " + source + " has been read successfuly from the inbox. now document must be saved " + destination);
        DocumentKeyIDWithKey documentKeyIDWithKey = getAnySecretKeyIDWithKeyFromKeyStore(userIDAuth);
        documentPersistenceService.get().encryptAndPersistDocument(documentKeyIDWithKey, getTheDocumentBucketPath(userIDAuth.getUserID(), destination), overwriteFlag,
                new SimplePayloadImpl(storageMetadata, dsDocument.getDocumentContent().getValue()));

        LOGGER.debug("now document must be removed from the inbox " + source);
        deleteDocumentFromInbox(userIDAuth, source);

        LOGGER.debug("finished readDocument from inbox for " + userIDAuth + " from " + source + " to " + destination);
        return new DSDocument(destination, dsDocument.getDocumentContent(), dsDocument.getDsDocumentMetaInfo());

    }

    @Override
    public PublicKeyJWK findPublicEncryptionKey(UserID userID) {
        KeyStoreAccess keyStoreAccess = getKeyStoreAccess(new UserIDAuth(userID, null));
        return keySourceService.findPublicEncryptionKey(keyStoreAccess);
    }

    @Override
    public void writeDocumentToInboxOfUser(UserID receiverUserID, DSDocument document, DocumentFQN destDocumentFQN) {
        DocumentKeyIDWithKey documentKeyIDWithKey = createNewAsymmetricGuardForUser(receiverUserID);

        SimpleStorageMetadataImpl storageMetadata = new SimpleStorageMetadataImpl();
        storageMetadata.mergeUserMetadata(document.getDsDocumentMetaInfo());
        storageMetadata.setSize(new Long(document.getDocumentContent().getValue().length));
        DocumentBucketPath documentBucketPath = new DocumentBucketPath(UserIDUtil.getInboxDirectory(receiverUserID).appendName(destDocumentFQN.getValue()));
        documentPersistenceService.get().encryptAndPersistDocument(
                documentKeyIDWithKey,
                documentBucketPath,
                OverwriteFlag.FALSE,
                new SimplePayloadImpl(storageMetadata, document.getDocumentContent().getValue()));

    }

    @Override
    public DSDocument readDocumentFromInbox(UserIDAuth userIDAuth, DocumentFQN source) {
        DocumentBucketPath inboxDocumentBucketPath = new DocumentBucketPath(UserIDUtil.getInboxDirectory(userIDAuth.getUserID()).appendName(source.getValue()));
        StorageMetadata storageMetadata = extendedStoreConnection.getStorageMetadata(inboxDocumentBucketPath);
        KeyStoreAccess keyStoreAccess = getKeyStoreAccess(userIDAuth);
        Payload payload = documentPersistenceService.get().loadAndDecryptDocument(storageMetadata, keyStoreAccess, inboxDocumentBucketPath);
        return new DSDocument(source, new DocumentContent(payload.getData()), new DSDocumentMetaInfo(payload.getStorageMetadata().getUserMetadata()));
    }

    @Override
    public void deleteDocumentFromInbox(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        DocumentBucketPath inboxDocumentBucketPath = new DocumentBucketPath(UserIDUtil.getInboxDirectory(userIDAuth.getUserID()).appendName(documentFQN.getValue()));
        bucketService.deletePlainFile(inboxDocumentBucketPath);
    }

    @Override
    public DocumentKeyIDWithKey get(KeyStoreAccess keyStoreAccess, DocumentKeyID documentKeyID) {
        DocumentGuardCache documentGuardCache = docusafeCacheWrapper.getDocumentGuardCache();
        String cacheKey = DocumentGuardCache.cacheKeyToString(keyStoreAccess, documentKeyID);
        PasswordAndDocumentKeyIDWithKey passwordAndDocumentKeyIDWithKeyFromCache = documentGuardCache.get(cacheKey);
        if (passwordAndDocumentKeyIDWithKeyFromCache != null) {
            if (passwordAndDocumentKeyIDWithKeyFromCache.getReadKeyPassword().equals(keyStoreAccess.getKeyStoreAuth().getReadKeyPassword())) {
                LOGGER.debug("return document key for cache key " + cacheKey);
                return documentGuardCache.get(cacheKey).getDocumentKeyIDWithKey();
            }
            // Password war falsch, wir lassen den Aufrufer abtauchen und die original Exception erhalten
            documentGuardCache.remove(cacheKey);
        }
        return null;
    }

    @Override
    public void put(KeyStoreAccess keyStoreAccess, DocumentKeyIDWithKey documentKeyIDWithKey) {
        DocumentGuardCache documentGuardCache = docusafeCacheWrapper.getDocumentGuardCache();
        String cacheKey = DocumentGuardCache.cacheKeyToString(keyStoreAccess, documentKeyIDWithKey.getDocumentKeyID());
        PasswordAndDocumentKeyIDWithKey passwordAndDocumentKeyIDWithKey = new PasswordAndDocumentKeyIDWithKey(keyStoreAccess.getKeyStoreAuth().getReadKeyPassword(), documentKeyIDWithKey);
        documentGuardCache.put(cacheKey, passwordAndDocumentKeyIDWithKey);
    }



    /**
     * PRIVATE STUFF
     * ===========================================================================================
     */


    private DocumentKeyIDWithKey createNewAsymmetricGuardForUser(UserID receiversUserID) {
        LOGGER.debug("start create asymmetric guard for " + receiversUserID);
        DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardService.createDocumentKeyIdWithKey();
        UserIDAuth receiverUserIDAuth = new UserIDAuth(receiversUserID, null);
        KeyStoreAccess receiverKeyStoreAccess = getKeyStoreAccess(receiverUserIDAuth);
        createCachedDocumentGuardFor(GuardKeyType.PUBLIC_KEY, receiverKeyStoreAccess, documentKeyIDWithKey, OverwriteFlag.FALSE);
        LOGGER.debug("finished create asymmetric guard for " + receiversUserID);
        return documentKeyIDWithKey;
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

    private void checkUserKeyPassword(UserIDAuth userIDAuth) {
        UserAuthCache userAuthCache = docusafeCacheWrapper != null ? docusafeCacheWrapper.getUserAuthCache() : null;
        if (userAuthCache != null) {
            LOGGER.debug("MemoryContext is used");

            ReadKeyPassword expectedFromCache = userAuthCache.get(userIDAuth.getUserID());
            if (expectedFromCache != null) {
                if (expectedFromCache.equals(userIDAuth.getReadKeyPassword())) {
                    LOGGER.debug("MemoryContext successful for " + userIDAuth.getUserID());
                    return;
                }
                // Password war falsch, also löschen und normale Prozedur laufen lassen
                // Koennte sich ja z.B. geändert haben.
                userAuthCache.remove(userIDAuth.getUserID());
            }
        }
        try {
            getAnySecretKeyIDWithKeyFromKeyStore(userIDAuth);
            if (userAuthCache != null) {
                userAuthCache.put(userIDAuth.getUserID(), userIDAuth.getReadKeyPassword());
            }
        } catch (SymmetricEncryptionException e) {
            throw new WrongPasswordException(userIDAuth.getUserID());
        } catch (BaseException e) {
            throw new UserIDDoesNotExistException(userIDAuth.getUserID());
        }
    }

    void createCachedDocumentGuardFor(GuardKeyType guardKeyType, KeyStoreAccess keyStoreAccess,
                                      DocumentKeyIDWithKey documentKeyIDWithKey,
                                      OverwriteFlag overwriteFlag) {

        documentGuardService.createDocumentGuardFor(guardKeyType, keyStoreAccess, documentKeyIDWithKey, overwriteFlag);
        if (guardKeyType.equals(GuardKeyType.SECRET_KEY)) {
            DocumentGuardCache documentGuardCache = docusafeCacheWrapper.getDocumentGuardCache();
            String cacheKey = DocumentGuardCache.cacheKeyToString(keyStoreAccess, documentKeyIDWithKey.getDocumentKeyID());
            documentGuardCache.put(cacheKey, new PasswordAndDocumentKeyIDWithKey(keyStoreAccess.getKeyStoreAuth().getReadKeyPassword(), documentKeyIDWithKey));
        }
        // else {
        //       guards für public Keys werden nicht gecached. Zum einen, weil das password nicht bekannt ist
        //       und zum anderen weil der Benutzer viele asymmetrische Guards haben kann.
        // }
    }

    private DocumentKeyIDWithKey getAnySecretKeyIDWithKeyFromKeyStore(UserIDAuth userIDAuth) {
        LOGGER.warn("do not reload keystore every time for encryption");
        KeyStoreAuth keyStoreAuth = UserIDUtil.getKeyStoreAuth(userIDAuth);
        BucketPath keyStorePath = UserIDUtil.getKeyStorePath(userIDAuth.getUserID());
        KeyStore keystore = keyStoreService.loadKeystore(keyStorePath, keyStoreAuth.getReadStoreHandler());
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStorePath, keyStoreAuth);
        SecretKeyIDWithKey secretKeyIDWithKey = KeyStore2KeySourceHelper.getRandomSecretKeyIDWithKey(keyStoreAccess, keystore);
        return new DocumentKeyIDWithKey(new DocumentKeyID(secretKeyIDWithKey.getKeyID().getValue()), new DocumentKey(secretKeyIDWithKey.getSecretKey()));
    }

    public static String showCache(DocumentSafeService instance) {
        if (!(instance instanceof DocumentSafeServiceImpl)) {
            throw new BaseException("Instance of DocuementSafeService is not of DocumentSafeServiceImpl but " + instance.getClass().getName());
        }
        DocumentSafeServiceImpl impl = (DocumentSafeServiceImpl) instance;
        if (impl.docusafeCacheWrapper == null) {
            return "(DocusafeCacheWrapper is null)";
        }
        return impl.docusafeCacheWrapper.toString();
    }
}
