package org.adorsys.resource.server.service;

import de.adorsys.resource.server.keyservice.SecretKeyGenerator;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.service.*;
import org.adorsys.encobject.utils.TestFsBlobStoreFactory;
import org.adorsys.encobject.utils.TestKeyUtils;
import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;
import org.adorsys.resource.server.basetypes.BucketName;
import org.adorsys.resource.server.basetypes.UserID;
import org.junit.*;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Created by peter on 02.01.18.
 */
public class UserKeyStoreServiceTest {

    private static String container = UserKeyStoreServiceTest.class.getSimpleName();
    private static BlobStoreContextFactory storeContextFactory;
    private static KeystorePersistence keystorePersistence;
    private static ContainerPersistence containerPersistence;

    @BeforeClass
    public static void beforeClass(){
        TestKeyUtils.turnOffEncPolicy();
        storeContextFactory = new TestFsBlobStoreFactory();
        keystorePersistence = new BlobStoreKeystorePersistence(storeContextFactory);
        containerPersistence = new ContainerPersistence(new BlobStoreConnection(storeContextFactory));

        try {
            containerPersistence.creteContainer(container);
        } catch (ContainerExistsException e) {
            Assume.assumeNoException(e);
        }

    }

    @AfterClass
    public static void afterClass(){
        try {
            if(containerPersistence!=null && containerPersistence.containerExists(container))
                containerPersistence.deleteContainer(container);
        } catch (UnknownContainerException e) {
            Assume.assumeNoException(e);
        }
    }

    @Test
    public void testStoreKeystore() throws NoSuchAlgorithmException, CertificateException, UnknownContainerException {
        String storeid = "AnysampleKeyStorePersistence";
        char[] storePass = "AnyaSimplePass".toCharArray();
        KeyStore keystore = TestKeyUtils.testSecretKeystore(storeid, storePass, "mainKey", "aSimpleSecretPass".toCharArray());
        Assume.assumeNotNull(keystore);
        keystorePersistence.saveKeyStore(keystore, TestKeyUtils.callbackHandlerBuilder(storeid, storePass).build(), new ObjectHandle(container, storeid));
        Assert.assertTrue(TestFsBlobStoreFactory.existsOnFs(container, storeid));
    }

    @Test
    public void test1() throws CertificateException, NoSuchAlgorithmException, UnknownContainerException {
        String keypasswordstring = "affe";
        String useridstring = "affe";
        String bucketnamestring = "affe";

        BlobStoreContextFactory blobStoreContextFactory = new TestFsBlobStoreFactory();
        BlobStoreKeystorePersistence blobStoreKeyStorePersistence = new BlobStoreKeystorePersistence(blobStoreContextFactory);
        UserKeyStoreService userKeyStoreService = new UserKeyStoreService(blobStoreKeyStorePersistence);
        UserID userID = new UserID(useridstring);
        CallbackHandler userKeyStoreHandler = new CallbackHandler() {
            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback call: callbacks) {
                    System.out.println("user key Store handler callback:" + call);
                }
            }
        };
        CallbackHandler keyPassHanlder = new PasswordCallbackHandler(keypasswordstring.toCharArray());
        BucketName bucketName = new BucketName(bucketnamestring);
        userKeyStoreService.createUserKeyStore(userID, userKeyStoreHandler, keyPassHanlder, bucketName);
    }
}
