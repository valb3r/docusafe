package org.adorsys.resource.server.service;

import org.adorsys.encobject.service.BlobStoreConnection;
import org.adorsys.encobject.service.ContainerExistsException;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.service.UnknownContainerException;
import org.adorsys.encobject.utils.TestFsBlobStoreFactory;
import org.adorsys.encobject.utils.TestKeyUtils;
import org.adorsys.resource.server.basetypes.UserID;
import org.adorsys.resource.server.persistence.ExtendedKeystorePersistence;
import org.adorsys.resource.server.persistence.basetypes.BucketName;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreAuth;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreID;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreName;
import org.junit.Assume;

import java.security.KeyStore;

/**
 * Created by peter on 02.01.18.
 */
public class KeyStoreServiceTest {

    private static String keystoreContainer = "keysotre-container-" + KeyStoreServiceTest.class.getSimpleName();
    private static TestFsBlobStoreFactory storeContextFactory;
    private static ExtendedKeystorePersistence keystorePersistence;
    private static ContainerPersistence containerPersistence;

    public static void beforeTest() {
        TestKeyUtils.turnOffEncPolicy();
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
        BucketName keyStoreBucketName = new BucketName(keystoreContainer);
        String keypasswordstring = "KeyPassword";
        String userpasswordstring = "userPassword";
        String useridstring = "UserPeter";
        KeyStoreID keyStoreID = new KeyStoreID("key-store-id-123");

        KeyStoreService keyStoreService = new KeyStoreService(keystorePersistence);
        UserID userID = new UserID(useridstring);
        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(keypasswordstring, userpasswordstring);
        KeyStoreName keyStoreName = keyStoreService.createKeyStore(keyStoreID, keyStoreAuth, keyStoreBucketName);
        KeyStore userKeyStore = keyStoreService.loadKeystore(keyStoreName, keyStoreAuth.getUserpass());
        return new KeyStoreStuff(userKeyStore, keystorePersistence, keyStoreBucketName, keyStoreID, keyStoreAuth, keyStoreName);
        // System.out.println(ShowKeyStore.toString(userKeyStore, keypasswordstring));
    }

    public static class KeyStoreStuff {
        public KeyStore keyStore;
        public ExtendedKeystorePersistence keystorePersistence;
        public BucketName keyStoreBucketName;
        public KeyStoreID keyStoreID;
        public KeyStoreAuth keyStoreAuth;
        public KeyStoreName keyStoreName;

        public KeyStoreStuff(KeyStore keyStore, ExtendedKeystorePersistence keystorePersistence, BucketName keyStoreBucketName, KeyStoreID keyStoreID, KeyStoreAuth keyStoreAuth, KeyStoreName keyStoreName) {
            this.keyStore = keyStore;
            this.keystorePersistence = keystorePersistence;
            this.keyStoreBucketName = keyStoreBucketName;
            this.keyStoreID = keyStoreID;
            this.keyStoreAuth = keyStoreAuth;
            this.keyStoreName = keyStoreName;
        }
    }
}
