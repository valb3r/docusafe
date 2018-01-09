package org.adorsys.resource.server.service;

import org.adorsys.encobject.service.BlobStoreConnection;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.utils.TestFsBlobStoreFactory;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.ExtendedKeystorePersistence;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreBucketName;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreID;
import org.adorsys.resource.server.persistence.basetypes.ReadKeyPassword;
import org.adorsys.resource.server.persistence.basetypes.ReadStorePassword;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreAccess;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreAuth;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreCreationConfig;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreLocation;

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

    public KeyStoreStuff createKeyStore(ReadStorePassword readStorePassword, ReadKeyPassword readKeyPassword, KeyStoreID keyStoreID) {
        return createKeyStore(keystorePersistence, keystoreContainer, readStorePassword, readKeyPassword, keyStoreID, null, null, null);
    }

    public KeyStoreStuff createKeyStore() {
        return createKeyStore(keystorePersistence, keystoreContainer, new ReadStorePassword("storePassword"), new ReadKeyPassword("keypassword"), new KeyStoreID("key-store-id-123"), null, null, null);
    }

    public KeyStoreStuff createKeyStore(ExtendedKeystorePersistence keystorePersistence, String keystoreContainer, ReadStorePassword readStorePassword, ReadKeyPassword readKeyPassword, KeyStoreID keyStoreID,
                                        Integer encKeyNumber, Integer signKeyNumber, Integer secretKeyNumber) {
        KeyStoreCreationConfig config = null;
        if (encKeyNumber != null) {
            config = new KeyStoreCreationConfig(encKeyNumber, signKeyNumber, secretKeyNumber, keyStoreID);
        }

        KeyStoreBucketName keyStoreBucketName = new KeyStoreBucketName(keystoreContainer);

        KeyStoreService keyStoreService = new KeyStoreService(keystorePersistence);
        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);
        KeyStoreLocation keyStoreLocation = keyStoreService.createKeyStore(keyStoreID, keyStoreAuth, keyStoreBucketName, config);
        KeyStore keyStore = keyStoreService.loadKeystore(keyStoreLocation, keyStoreAuth.getReadStoreHandler());
        // System.out.println(ShowKeyStore.toString(userKeyStore, keypasswordstring));
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
