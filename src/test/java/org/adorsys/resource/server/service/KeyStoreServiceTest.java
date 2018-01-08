package org.adorsys.resource.server.service;

import org.adorsys.encobject.service.BlobStoreConnection;
import org.adorsys.encobject.service.ContainerExistsException;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.service.UnknownContainerException;
import org.adorsys.encobject.utils.TestFsBlobStoreFactory;
import org.adorsys.resource.server.persistence.ExtendedKeystorePersistence;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreBucketName;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreID;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreAccess;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreAuth;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreLocation;
import org.junit.Assume;

import java.security.KeyStore;

/**
 * Created by peter on 02.01.18.
 */
public class KeyStoreServiceTest {

    private static String keystoreContainer = "keystore-container-" + KeyStoreServiceTest.class.getSimpleName();
    private static TestFsBlobStoreFactory storeContextFactory;
    private static ExtendedKeystorePersistence keystorePersistence;
    private static ContainerPersistence containerPersistence;

    public static void beforeTest() {
        storeContextFactory = new TestFsBlobStoreFactory();
        keystorePersistence = new ExtendedKeystorePersistence(storeContextFactory);
        containerPersistence = new ContainerPersistence(new BlobStoreConnection(storeContextFactory));

        try {
            containerPersistence.creteContainer(keystoreContainer);
        } catch (ContainerExistsException e) {
            Assume.assumeNoException(e);
        }

    }

    public static void afterTest() {
        try {
            if (containerPersistence != null && containerPersistence.containerExists(keystoreContainer))
                containerPersistence.deleteContainer(keystoreContainer);
        } catch (UnknownContainerException e) {
            Assume.assumeNoException(e);
        }
    }

    public KeyStoreStuff createKeyStore() {
        return createKeyStore("userpassword", "keypassword", new KeyStoreID("key-store-id-123"));
    }

        public KeyStoreStuff createKeyStore(String keyPassword, String userPassword, KeyStoreID keyStoreID) {
        KeyStoreBucketName keyStoreBucketName = new KeyStoreBucketName(keystoreContainer);

        KeyStoreService keyStoreService = new KeyStoreService(keystorePersistence);
        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(keyPassword, userPassword);
        KeyStoreLocation keyStoreLocation = keyStoreService.createKeyStore(keyStoreID, keyStoreAuth, keyStoreBucketName);
        KeyStore userKeyStore = keyStoreService.loadKeystore(keyStoreLocation, keyStoreAuth.getUserpass());
        return new KeyStoreStuff(userKeyStore, keystorePersistence, keyStoreID, new KeyStoreAccess(keyStoreLocation, keyStoreAuth));
        // System.out.println(ShowKeyStore.toString(userKeyStore, keypasswordstring));
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
}
