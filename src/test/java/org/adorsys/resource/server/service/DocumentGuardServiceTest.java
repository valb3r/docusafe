package org.adorsys.resource.server.service;

import org.adorsys.encobject.service.BlobStoreConnection;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.adorsys.encobject.service.ContainerExistsException;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.service.UnknownContainerException;
import org.adorsys.encobject.utils.TestFsBlobStoreFactory;
import org.adorsys.encobject.utils.TestKeyUtils;
import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;
import org.adorsys.resource.server.basetypes.DocumentGuardName;
import org.adorsys.resource.server.complextypes.DocumentGuard;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.ExtendedKeystorePersistence;
import org.adorsys.resource.server.persistence.ExtendedObjectPersistence;
import org.adorsys.resource.server.persistence.basetypes.BucketName;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreID;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreName;
import org.adorsys.resource.server.utils.HexUtil;
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
    private static String keystoreContainer = "keysotre-container-" + DocumentGuardServiceTest.class.getSimpleName();
    private static BlobStoreContextFactory keystoreContextFactory;
    private static ExtendedKeystorePersistence keystorePersistence;
    private static ContainerPersistence keystoreContainerPersistence;

    private static BlobStoreContextFactory guardContextFactory;
    private static ContainerPersistence guardContainerPersistence;
    private static ExtendedObjectPersistence guardExtendedPersistence;
    private static String keypasswordstring = "KeyPassword";

    @BeforeClass
    public static void beforeClass() {
        TestKeyUtils.turnOffEncPolicy();

        keystoreContextFactory = new TestFsBlobStoreFactory();
        keystorePersistence = new ExtendedKeystorePersistence(keystoreContextFactory);
        keystoreContainerPersistence = new ContainerPersistence(new BlobStoreConnection(keystoreContextFactory));

        guardContextFactory = new TestFsBlobStoreFactory();
        guardContainerPersistence = new ContainerPersistence(new BlobStoreConnection(guardContextFactory));
        guardExtendedPersistence = new ExtendedObjectPersistence(new BlobStoreConnection(guardContextFactory));

        try {
            keystoreContainerPersistence.creteContainer(keystoreContainer);
        } catch (ContainerExistsException e) {
            Assume.assumeNoException(e);
        }

    }

    @AfterClass
    public static void afterClass() {
        try {
            if (keystoreContainerPersistence != null && keystoreContainerPersistence.containerExists(keystoreContainer))
                keystoreContainerPersistence.deleteContainer(keystoreContainer);
        } catch (UnknownContainerException e) {
            Assume.assumeNoException(e);
        }
    }

    @Test
    public void testCreateDocumentGuard() {
        createDocumentGuard();
    }

    @Test
    public void testCreateAndLoadDocumentGuard() {
        try {
            DocumentGuardName guardName = createDocumentGuard();
            DocumentGuardService documentGuardService = new DocumentGuardService(keystorePersistence, guardExtendedPersistence);
            CallbackHandler userKeyStoreHandler = new PasswordCallbackHandler(keypasswordstring.toCharArray());
            CallbackHandler keyPassHandler = new PasswordCallbackHandler(keypasswordstring.toCharArray());
            DocumentGuard documentGuard = documentGuardService.loadDocumentGuard(guardName, userKeyStoreHandler, keyPassHandler);
            System.out.println("key des Guards ist :" + documentGuard.getDocumentKey());
            System.out.println("LOAD DocumentKey:" + HexUtil.conventBytesToHexString(documentGuard.getDocumentKey().getSecretKey().getEncoded()));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }
    
    private DocumentGuardName createDocumentGuard() {
        try {
            KeyStoreID keyStoreID = new KeyStoreID("another-key-store-id-456");

            CallbackHandler userKeyStoreHandler = new PasswordCallbackHandler(keypasswordstring.toCharArray());
            CallbackHandler keyPassHandler = new PasswordCallbackHandler(keypasswordstring.toCharArray());
            KeyStoreService keyStoreService = new KeyStoreService(keystorePersistence);
            KeyStoreName keyStoreName = keyStoreService.createKeyStore(keyStoreID, userKeyStoreHandler, keyPassHandler, new BucketName(keystoreContainer));

            {
                KeyStore userKeyStore = keyStoreService.loadKeystore(keyStoreName, userKeyStoreHandler);
                Assert.assertEquals("Number of entries of KeyStore is 15", 15, userKeyStore.size());
            }

            DocumentGuardService documentGuardService = new DocumentGuardService(keystorePersistence, guardExtendedPersistence);
            DocumentGuardName guardName = documentGuardService.createDocumentGuard(keyStoreName, userKeyStoreHandler, keyPassHandler);
            System.out.println("user guard erzeugt:" + guardName);
            return guardName;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
