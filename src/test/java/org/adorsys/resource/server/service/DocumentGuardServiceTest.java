package org.adorsys.resource.server.service;

import org.adorsys.encobject.service.BlobStoreConnection;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.utils.TestFsBlobStoreFactory;
import org.adorsys.resource.server.basetypes.DocumentGuardName;
import org.adorsys.resource.server.complextypes.DocumentGuard;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.ExtendedKeystorePersistence;
import org.adorsys.resource.server.persistence.ExtendedObjectPersistence;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreAuth;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreBucketName;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreID;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreName;
import org.adorsys.resource.server.utils.HexUtil;
import org.junit.Assert;

import java.security.KeyStore;

/**
 * Created by peter on 02.01.18.
 */
public class DocumentGuardServiceTest {

    private static BlobStoreContextFactory guardContextFactory;
    private static ContainerPersistence guardContainerPersistence;
    private static ExtendedObjectPersistence guardExtendedPersistence;

    public static void beforeClass() {
        guardContextFactory = new TestFsBlobStoreFactory();
        guardContainerPersistence = new ContainerPersistence(new BlobStoreConnection(guardContextFactory));
        guardExtendedPersistence = new ExtendedObjectPersistence(new BlobStoreConnection(guardContextFactory));

    }
    public static void afterClass() {

    }

    public DocumentGuardStuff testCreateDocumentGuard(KeyStoreAuth keyStoreAuth, ExtendedKeystorePersistence keystorePersistence, KeyStoreBucketName keystoreBucketName, KeyStoreID keyStoreID) {
        try {
            KeyStoreService keyStoreService = new KeyStoreService(keystorePersistence);
            KeyStoreName keyStoreName = keyStoreService.createKeyStore(keyStoreID, keyStoreAuth, keystoreBucketName);

            {
                KeyStore userKeyStore = keyStoreService.loadKeystore(keyStoreName, keyStoreAuth.getUserpass());
                Assert.assertEquals("Number of entries of KeyStore is 15", 15, userKeyStore.size());
            }

            DocumentGuardService documentGuardService = new DocumentGuardService(keystorePersistence, guardExtendedPersistence);
            DocumentGuardName guardName = documentGuardService.createDocumentGuard(keyStoreName, keyStoreAuth);
            System.out.println("user guard erzeugt:" + guardName);
            return new DocumentGuardStuff(documentGuardService, guardName);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public DocumentGuard testLoadDocumentGuard(
            KeyStoreAuth keyStoreAuth,
            ExtendedKeystorePersistence keystorePersistence,
            DocumentGuardName documentGuardName) {
        try {
            DocumentGuardService documentGuardService = new DocumentGuardService(keystorePersistence, guardExtendedPersistence);
            DocumentGuard documentGuard = documentGuardService.loadDocumentGuard(documentGuardName, keyStoreAuth);
            System.out.println("key des Guards ist :" + documentGuard.getDocumentKey());
            System.out.println("LOAD DocumentKey:" + HexUtil.conventBytesToHexString(documentGuard.getDocumentKey().getSecretKey().getEncoded()));
            return documentGuard;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }

    public static class DocumentGuardStuff {
        public DocumentGuardService documentGuardService;
        public DocumentGuardName documentGuardName;
        public DocumentGuardStuff(DocumentGuardService documentGuardService, DocumentGuardName documentGuardName) {
            this.documentGuardService = documentGuardService;
            this.documentGuardName = documentGuardName;
        }
    }
}
