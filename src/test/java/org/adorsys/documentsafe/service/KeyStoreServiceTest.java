package org.adorsys.documentsafe.service;

import org.adorsys.documentsafe.layer00common.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer02service.InterfaceKeyStoreService;
import org.adorsys.documentsafe.layer02service.impl.KeyStoreService;
import org.adorsys.documentsafe.layer02service.types.ReadKeyPassword;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.documentsafe.layer02service.generators.KeyStoreCreationConfig;
import org.adorsys.encobject.service.BlobStoreConnection;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.utils.TestFsBlobStoreFactory;
import org.adorsys.documentsafe.layer01persistence.ExtendedKeystorePersistence;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.KeyStoreBucketPath;
import org.adorsys.documentsafe.layer01persistence.types.KeyStoreID;
import org.adorsys.documentsafe.layer02service.types.ReadStorePassword;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAuth;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.KeyStoreLocation;

import java.security.KeyStore;

/**
 * Created by peter on 02.01.18.
 */
public class KeyStoreServiceTest {

    private static String keystoreContainer = "keystore-container-" + KeyStoreServiceTest.class.getSimpleName();
    private static ExtendedKeystorePersistence keystorePersistence;

    public static void beforeTest() {
        keystorePersistence = createKeyStorePersistenceForContainer(keystoreContainer);
    }

    public static void afterTest() {
        removeContainer(keystoreContainer);
    }

    public KeyStoreStuff createKeyStore() {
        return createKeyStore(keystorePersistence, keystoreContainer, new ReadStorePassword("storePassword"), new ReadKeyPassword("keypassword"), new KeyStoreID("key-store-id-123"), null);
    }

    public KeyStoreStuff createKeyStore(ExtendedKeystorePersistence keystorePersistence,
                                        String keystoreContainer,
                                        ReadStorePassword readStorePassword,
                                        ReadKeyPassword readKeyPassword,
                                        KeyStoreID keyStoreID,
                                        KeyStoreCreationConfig config) {
        KeyStoreBucketPath keyStoreBucketPath = new KeyStoreBucketPath(keystoreContainer);
        InterfaceKeyStoreService keyStoreService = new KeyStoreService(keystorePersistence);
        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);
        KeyStoreLocation keyStoreLocation = keyStoreService.createKeyStore(keyStoreID, keyStoreAuth, keyStoreBucketPath, config);
        KeyStore keyStore = keyStoreService.loadKeystore(keyStoreLocation, keyStoreAuth.getReadStoreHandler());
        return new KeyStoreStuff(keyStore, keystorePersistence, keyStoreID, new KeyStoreAccess(keyStoreLocation, keyStoreAuth));
    }


    public static class KeyStoreStuff {
        public final KeyStore keyStore;
        public final ExtendedKeystorePersistence keystorePersistence;
        public final KeyStoreAccess keyStoreAccess;

        public KeyStoreStuff(KeyStore keyStore, ExtendedKeystorePersistence keystorePersistence, KeyStoreID keyStoreID, KeyStoreAccess keyStoreAccess) {
            this.keyStore = keyStore;
            this.keystorePersistence = keystorePersistence;
            this.keyStoreAccess = keyStoreAccess;
        }
    }

    public static ExtendedKeystorePersistence createKeyStorePersistenceForContainer(String container) {
        try {
            TestFsBlobStoreFactory storeContextFactory = new TestFsBlobStoreFactory();
            ExtendedKeystorePersistence keystorePersistence = new ExtendedKeystorePersistence(storeContextFactory);
            ContainerPersistence containerPersistence = new ContainerPersistence(new BlobStoreConnection(storeContextFactory));
            containerPersistence.creteContainer(container);
            return keystorePersistence;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public static void removeContainer(String container) {
        try {
            TestFsBlobStoreFactory storeContextFactory = new TestFsBlobStoreFactory();
            ContainerPersistence containerPersistence = new ContainerPersistence(new BlobStoreConnection(storeContextFactory));
            containerPersistence.deleteContainer(container);
        } catch (Exception e) {
            // ignore this
        }
    }

}
