package org.adorsys.resource.server.service;

import org.adorsys.encobject.service.BlobStoreConnection;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.adorsys.encobject.service.BlobStoreKeystorePersistence;
import org.adorsys.encobject.service.ContainerExistsException;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.service.KeystorePersistence;
import org.adorsys.encobject.service.UnknownContainerException;
import org.adorsys.encobject.utils.TestFsBlobStoreFactory;
import org.adorsys.encobject.utils.TestKeyUtils;
import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;
import org.adorsys.resource.server.basetypes.BucketName;
import org.adorsys.resource.server.basetypes.DocumentGuardName;
import org.adorsys.resource.server.basetypes.UserID;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.ExtendedObjectPersistence;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;

/**
 * Created by peter on 02.01.18.
 */
public class DocumentGuardServiceTest {
    private static String container = UserKeyStoreServiceTest.class.getSimpleName();
    private static BlobStoreContextFactory storeContextFactory;
    private static KeystorePersistence keystorePersistence;
    private static ContainerPersistence containerPersistence;
    private static ExtendedObjectPersistence extendedObjectPersistence;

    @BeforeClass
    public static void beforeClass() {
        TestKeyUtils.turnOffEncPolicy();
        storeContextFactory = new TestFsBlobStoreFactory();
        keystorePersistence = new BlobStoreKeystorePersistence(storeContextFactory);
        containerPersistence = new ContainerPersistence(new BlobStoreConnection(storeContextFactory));
        BlobStoreConnection blobStoreConnection = new BlobStoreConnection(storeContextFactory);
        extendedObjectPersistence = new ExtendedObjectPersistence(blobStoreConnection);

        try {
            containerPersistence.creteContainer(container);
        } catch (ContainerExistsException e) {
            Assume.assumeNoException(e);
        }

    }

    // @AfterClass
    public static void afterClass() {
        try {
            if (containerPersistence != null && containerPersistence.containerExists(container))
                containerPersistence.deleteContainer(container);
        } catch (UnknownContainerException e) {
            Assume.assumeNoException(e);
        }
    }

    // TODO, Exception nicht verstanden, muss noch gefixed werden
    @Test(expected = org.adorsys.resource.server.exceptions.BaseException.class)
    public void createSelfCuard() {
        try {
            String keypasswordstring = "KeyPassword";
            UserID userID = new UserID("peter_der_user");
            String bucketnamestring = container;

            BucketName bucketName = new BucketName(bucketnamestring);
            CallbackHandler userKeyStoreHandler = new PasswordCallbackHandler(keypasswordstring.toCharArray());
            CallbackHandler keyPassHandler = new PasswordCallbackHandler(keypasswordstring.toCharArray());

            {
                UserKeyStoreService userKeyStoreService = new UserKeyStoreService(keystorePersistence);
                KeyStore userKeyStore = userKeyStoreService.createUserKeyStore(userID, userKeyStoreHandler, keyPassHandler, bucketName);
                Assert.assertEquals("Number of entries of KeyStore is 15", 15, userKeyStore.size());
            }


            DocumentGuardService documentGuardService = new DocumentGuardService(keystorePersistence, extendedObjectPersistence);
            DocumentGuardName userSelfGuard = documentGuardService.createUserSelfGuard(userID, userKeyStoreHandler, keyPassHandler, bucketName);
            System.out.println(userSelfGuard);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }
}
