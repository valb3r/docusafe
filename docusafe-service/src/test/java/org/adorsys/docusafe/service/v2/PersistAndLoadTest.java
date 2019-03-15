package org.adorsys.docusafe.service.v2;

import com.googlecode.catchexception.CatchException;
import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.docusafe.service.DocumentGuardService;
import org.adorsys.docusafe.service.DocumentPersistenceService;
import org.adorsys.docusafe.service.impl.DocumentGuardServiceImpl;
import org.adorsys.docusafe.service.impl.DocumentPersistenceServiceImpl;
import org.adorsys.docusafe.service.impl.GuardKeyType;
import org.adorsys.docusafe.service.types.DocumentKey;
import org.adorsys.docusafe.service.types.DocumentKeyID;
import org.adorsys.docusafe.service.types.complextypes.DocumentBucketPath;
import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.*;
import org.adorsys.encobject.exceptions.KeyStoreConfigException;
import org.adorsys.encobject.exceptions.SymmetricEncryptionException;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.api.KeyStore2KeySourceHelper;
import org.adorsys.encobject.service.api.KeyStoreService;
import org.adorsys.encobject.service.impl.KeyStoreServiceImpl;
import org.adorsys.encobject.service.impl.SimplePayloadImpl;
import org.adorsys.encobject.service.impl.generator.KeyStoreCreationConfigImpl;
import org.adorsys.encobject.types.OverwriteFlag;
import org.adorsys.encobject.types.SecretKeyIDWithKey;
import org.adorsys.jkeygen.keystore.KeyStoreType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyStore;
import java.util.HashSet;
import java.util.Set;

public class PersistAndLoadTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(PersistAndLoadTest.class);

    private final ExtendedStoreConnection extendedStoreConnection = ExtendedStoreConnectionFactory.get();
    private final KeyStoreService keyStoreService = new KeyStoreServiceImpl(extendedStoreConnection);
    private final DocumentPersistenceService documentPersistenceService = new DocumentPersistenceServiceImpl(extendedStoreConnection, null);
    private final DocumentGuardService documentGuardService = new DocumentGuardServiceImpl(extendedStoreConnection);

    private Set<BucketDirectory> directoriesToDeleteAfterTest = new HashSet<>();

    @After
    public void afterTest() {
        directoriesToDeleteAfterTest.forEach(directory -> extendedStoreConnection.deleteContainer(directory));
    }

    @Test
    public void symmetricEncryption() {

        LOGGER.debug("create keystore with only one secret key");
        BucketPath keyStorePath = new BucketPath("symmetrictest/keystoredirectory/keystore");
        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(new ReadStorePassword("readStorePassword"), new ReadKeyPassword("readKeyPassword"));
        extendedStoreConnection.createContainer(keyStorePath.getBucketDirectory());
        directoriesToDeleteAfterTest.add(keyStorePath.getBucketDirectory());
        keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, keyStorePath, new KeyStoreCreationConfigImpl(0, 0, 1));

        KeyStore keyStore = keyStoreService.loadKeystore(keyStorePath, keyStoreAuth.getReadStoreHandler());
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStorePath, keyStoreAuth);

        LOGGER.debug("create document");
        String documentContentString = "Just keep it simple and stupid - > kiss";
        DocumentBucketPath documentBucketPath = new DocumentBucketPath("symmetrictest/documentdirectory/document.txt");
        SecretKeyIDWithKey randomSecretKeyIDWithKey = KeyStore2KeySourceHelper.getRandomSecretKeyIDWithKey(keyStoreAccess, keyStore);
        DocumentKeyIDWithKey documentKeyIDWithKey = new DocumentKeyIDWithKey(new DocumentKeyID(randomSecretKeyIDWithKey.getKeyID().getValue()), new DocumentKey(randomSecretKeyIDWithKey.getSecretKey()));
        Payload payloadWrite = new SimplePayloadImpl(documentContentString.getBytes());
        extendedStoreConnection.createContainer(documentBucketPath.getBucketDirectory());
        directoriesToDeleteAfterTest.add(documentBucketPath.getBucketDirectory());
        documentPersistenceService.encryptAndPersistDocument(documentKeyIDWithKey, documentBucketPath, OverwriteFlag.FALSE, payloadWrite);

        LOGGER.debug("load document again");
        StorageMetadata storageMetadata = extendedStoreConnection.getStorageMetadata(documentBucketPath);
        Payload payloadRead = documentPersistenceService.loadAndDecryptDocument(storageMetadata, keyStoreAccess, documentBucketPath);

        Assert.assertArrayEquals(payloadWrite.getData(), payloadRead.getData());
    }


    @Test
    public void symmetricEncryptionWrongKeyStoreConfig() {

        LOGGER.debug("create keystore with only one secret key");
        BucketPath keyStorePath = new BucketPath("symmetrictest/keystoredirectory/keystore");
        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(new ReadStorePassword("readStorePassword"), new ReadKeyPassword("readKeyPassword"));
        extendedStoreConnection.createContainer(keyStorePath.getBucketDirectory());
        directoriesToDeleteAfterTest.add(keyStorePath.getBucketDirectory());
        CatchException.catchException(() -> keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, keyStorePath, new KeyStoreCreationConfigImpl(0, 0, 0)));
        Assert.assertTrue(CatchException.caughtException() instanceof KeyStoreConfigException);
    }

    @Test
    public void symmetricEncryptionWrongNoSecertKeyInKeyStore() {

        LOGGER.debug("create keystore with only one secret key");
        BucketPath keyStorePath = new BucketPath("symmetrictest/keystoredirectory/keystore");
        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(new ReadStorePassword("readStorePassword"), new ReadKeyPassword("readKeyPassword"));
        extendedStoreConnection.createContainer(keyStorePath.getBucketDirectory());
        directoriesToDeleteAfterTest.add(keyStorePath.getBucketDirectory());
        keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, keyStorePath, new KeyStoreCreationConfigImpl(1, 0, 0));

        KeyStore keyStore = keyStoreService.loadKeystore(keyStorePath, keyStoreAuth.getReadStoreHandler());
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStorePath, keyStoreAuth);
        CatchException.catchException(() -> KeyStore2KeySourceHelper.getRandomSecretKeyIDWithKey(keyStoreAccess, keyStore));
        Assert.assertTrue(CatchException.caughtException() instanceof SymmetricEncryptionException);
    }

    @Test
    public void asymmetricEncryption() {

        LOGGER.debug("create keystore with only one public and private key");
        BucketPath keyStorePath = new BucketPath("asymmetrictest/keystoredirectory/keystore");
        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(new ReadStorePassword("readStorePassword"), new ReadKeyPassword("readKeyPassword"));
        extendedStoreConnection.createContainer(keyStorePath.getBucketDirectory());
        directoriesToDeleteAfterTest.add(keyStorePath.getBucketDirectory());
        keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, keyStorePath, new KeyStoreCreationConfigImpl(1, 0, 0));

        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStorePath, keyStoreAuth);

        LOGGER.debug("create document guard");
        DocumentKeyIDWithKey documentKeyIdWithKey = documentGuardService.createDocumentKeyIdWithKey();
        documentGuardService.createDocumentGuardFor(GuardKeyType.PUBLIC_KEY, keyStoreAccess, documentKeyIdWithKey, OverwriteFlag.FALSE);


        LOGGER.debug("encrypt the document with the key inside the document guard");
        String documentContentString = "Just keep it simple and stupid - > kiss";
        DocumentBucketPath documentBucketPath = new DocumentBucketPath("asymmetrictest/documentdirectory/document.txt");
        Payload payloadWrite = new SimplePayloadImpl(documentContentString.getBytes());
        documentPersistenceService.encryptAndPersistDocument(documentKeyIdWithKey, documentBucketPath, OverwriteFlag.FALSE, payloadWrite);

        LOGGER.debug("decrypt the document with the key inside the document guard can not be possible without correct keystore password");
        StorageMetadata storageMetadata = extendedStoreConnection.getStorageMetadata(documentBucketPath);
        KeyStoreAccess keyStoreAccessWithWrongPassword = new KeyStoreAccess(keyStorePath, new KeyStoreAuth(keyStoreAccess.getKeyStoreAuth().getReadStorePassword(), new ReadKeyPassword("")));
        CatchException.catchException(() -> documentPersistenceService.loadAndDecryptDocument(storageMetadata, keyStoreAccessWithWrongPassword, documentBucketPath));
        Assert.assertTrue(CatchException.caughtException() instanceof BaseException);

        LOGGER.debug("decrypt the document with the key inside the document guard");
        Payload payloadRead = documentPersistenceService.loadAndDecryptDocument(storageMetadata, keyStoreAccess, documentBucketPath);

        Assert.assertArrayEquals(payloadWrite.getData(), payloadRead.getData());
    }

    @Test
    public void symmetricEncryptionStoreTwice() {

        LOGGER.debug("create keystore with only one secret key");
        BucketPath keyStorePath = new BucketPath("symmetrictest/keystoredirectory/keystore");
        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(new ReadStorePassword("readStorePassword"), new ReadKeyPassword("readKeyPassword"));
        extendedStoreConnection.createContainer(keyStorePath.getBucketDirectory());
        directoriesToDeleteAfterTest.add(keyStorePath.getBucketDirectory());
        keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, keyStorePath, new KeyStoreCreationConfigImpl(0, 0, 1));

        KeyStore keyStore = keyStoreService.loadKeystore(keyStorePath, keyStoreAuth.getReadStoreHandler());
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStorePath, keyStoreAuth);

        LOGGER.debug("create document");
        String documentContentString = "Just keep it simple and stupid - > kiss";
        DocumentBucketPath documentBucketPath = new DocumentBucketPath("symmetrictest/documentdirectory/document.txt");
        SecretKeyIDWithKey randomSecretKeyIDWithKey = KeyStore2KeySourceHelper.getRandomSecretKeyIDWithKey(keyStoreAccess, keyStore);
        DocumentKeyIDWithKey documentKeyIDWithKey = new DocumentKeyIDWithKey(new DocumentKeyID(randomSecretKeyIDWithKey.getKeyID().getValue()), new DocumentKey(randomSecretKeyIDWithKey.getSecretKey()));
        extendedStoreConnection.createContainer(documentBucketPath.getBucketDirectory());
        directoriesToDeleteAfterTest.add(documentBucketPath.getBucketDirectory());

        // the following methods are braced, because the payload will contain the encryption metainformation
        // these informations must not be set twice
        {
            Payload payloadWrite = new SimplePayloadImpl(documentContentString.getBytes());
            documentPersistenceService.encryptAndPersistDocument(documentKeyIDWithKey, documentBucketPath, OverwriteFlag.FALSE, payloadWrite);
        }
        {
            Payload payloadWrite = new SimplePayloadImpl(documentContentString.getBytes());
            CatchException.catchException(() -> documentPersistenceService.encryptAndPersistDocument(documentKeyIDWithKey, documentBucketPath, OverwriteFlag.FALSE, payloadWrite));
            Assert.assertTrue(CatchException.caughtException() instanceof BaseException);
        }
        {
            Payload payloadWrite = new SimplePayloadImpl(documentContentString.getBytes());
            documentPersistenceService.encryptAndPersistDocument(documentKeyIDWithKey, documentBucketPath, OverwriteFlag.TRUE, payloadWrite);
        }
    }
}