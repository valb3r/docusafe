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
import org.junit.AfterClass;
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
    //    private static String container = UserKeyStoreServiceTest.class.getSimpleName();
    private static String guardContainer = "guard";
    private static String keystoreContainer = "keystore";
    private static BlobStoreContextFactory keystoreContextFactory;
    private static KeystorePersistence keystorePersistence;
    private static ContainerPersistence keystoreContainerPersistence;

    private static BlobStoreContextFactory guardContextFactory;
    private static ContainerPersistence guardContainerPersistence;
    private static ExtendedObjectPersistence guardExtendedPersistence;

    @BeforeClass
    public static void beforeClass() {
        TestKeyUtils.turnOffEncPolicy();

        keystoreContextFactory = new TestFsBlobStoreFactory();
        keystorePersistence = new BlobStoreKeystorePersistence(keystoreContextFactory);
        keystoreContainerPersistence = new ContainerPersistence(new BlobStoreConnection(keystoreContextFactory));

        guardContextFactory = new TestFsBlobStoreFactory();
        guardContainerPersistence = new ContainerPersistence(new BlobStoreConnection(guardContextFactory));
        guardExtendedPersistence = new ExtendedObjectPersistence(new BlobStoreConnection(guardContextFactory));

        try {
            keystoreContainerPersistence.creteContainer(keystoreContainer);
            guardContainerPersistence.creteContainer(guardContainer);
        } catch (ContainerExistsException e) {
            Assume.assumeNoException(e);
        }

    }

    @AfterClass
    public static void afterClass() {
        try {
            if (keystoreContainerPersistence != null && keystoreContainerPersistence.containerExists(keystoreContainer))
                keystoreContainerPersistence.deleteContainer(keystoreContainer);
            if (guardContainerPersistence != null && guardContainerPersistence.containerExists(guardContainer))
                guardContainerPersistence.deleteContainer(guardContainer);
        } catch (UnknownContainerException e) {
            Assume.assumeNoException(e);
        }
    }

    @Test
    public void createSelfCuard() {
        try {
            String keypasswordstring = "KeyPassword";
            UserID userID = new UserID("peter_der_user");

            CallbackHandler userKeyStoreHandler = new PasswordCallbackHandler(keypasswordstring.toCharArray());
            CallbackHandler keyPassHandler = new PasswordCallbackHandler(keypasswordstring.toCharArray());

            {
                UserKeyStoreService userKeyStoreService = new UserKeyStoreService(keystorePersistence);
                KeyStore userKeyStore = userKeyStoreService.createUserKeyStore(userID, userKeyStoreHandler, keyPassHandler, new BucketName(keystoreContainer));
                Assert.assertEquals("Number of entries of KeyStore is 15", 15, userKeyStore.size());
            }

            DocumentGuardService documentGuardService = new DocumentGuardService(keystorePersistence, guardExtendedPersistence);
            DocumentGuardName userSelfGuard = documentGuardService.createUserSelfGuard(userID, userKeyStoreHandler, keyPassHandler, new BucketName(keystoreContainer), new BucketName(guardContainer));
            System.out.println("user guard erzeugt:" + userSelfGuard);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }
}
