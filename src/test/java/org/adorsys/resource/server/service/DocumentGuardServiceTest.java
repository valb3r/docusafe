package org.adorsys.resource.server.service;

import org.adorsys.encobject.service.BlobStoreConnection;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.utils.TestFsBlobStoreFactory;
import org.adorsys.resource.server.persistence.complextypes.DocumentGuard;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.ExtendedKeystorePersistence;
import org.adorsys.resource.server.persistence.ExtendedObjectPersistence;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreID;
import org.adorsys.resource.server.persistence.complextypes.DocumentGuardLocation;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreAuth;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreLocation;
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

    public DocumentGuardStuff testCreateDocumentGuard(KeyStoreAuth keyStoreAuth, ExtendedKeystorePersistence keystorePersistence, KeyStoreLocation keystoreLocation, KeyStoreID keyStoreID) {
        try {
            KeyStoreService keyStoreService = new KeyStoreService(keystorePersistence);
            KeyStoreLocation keyStoreLocation = keyStoreService.createKeyStore(keystoreLocation.getKeyStoreID(), keyStoreAuth, keystoreLocation.getKeyStoreBucketName());

            {
                KeyStore userKeyStore = keyStoreService.loadKeystore(keyStoreLocation, keyStoreAuth.getUserpass());
                Assert.assertEquals("Number of entries of KeyStore is 15", 15, userKeyStore.size());
            }

            DocumentGuardService documentGuardService = new DocumentGuardService(keystorePersistence, guardExtendedPersistence);
            DocumentGuardLocation guardName = documentGuardService.createDocumentGuard(keyStoreLocation, keyStoreAuth);
            System.out.println("user guard erzeugt:" + guardName);
            return new DocumentGuardStuff(documentGuardService, guardName);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public DocumentGuard testLoadDocumentGuard(
            KeyStoreAuth keyStoreAuth,
            ExtendedKeystorePersistence keystorePersistence,
            DocumentGuardLocation documentGuardLocation) {
        try {
            DocumentGuardService documentGuardService = new DocumentGuardService(keystorePersistence, guardExtendedPersistence);
            DocumentGuard documentGuard = documentGuardService.loadDocumentGuard(documentGuardLocation, keyStoreAuth);
            System.out.println("key des Guards ist :" + documentGuard.getDocumentKey());
            System.out.println("LOAD DocumentKey:" + HexUtil.conventBytesToHexString(documentGuard.getDocumentKey().getSecretKey().getEncoded()));
            return documentGuard;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }

    public static class DocumentGuardStuff {
        public DocumentGuardService documentGuardService;
        public DocumentGuardLocation documentGuardLocation;
        public DocumentGuardStuff(DocumentGuardService documentGuardService, DocumentGuardLocation documentGuardLocation) {
            this.documentGuardService = documentGuardService;
            this.documentGuardLocation = documentGuardLocation;
        }
    }
}
