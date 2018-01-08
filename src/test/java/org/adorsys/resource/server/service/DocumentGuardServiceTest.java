package org.adorsys.resource.server.service;

import org.adorsys.encobject.service.BlobStoreConnection;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.utils.TestFsBlobStoreFactory;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.ExtendedKeystorePersistence;
import org.adorsys.resource.server.persistence.ExtendedObjectPersistence;
import org.adorsys.resource.server.persistence.basetypes.DocumentKeyID;
import org.adorsys.resource.server.persistence.complextypes.DocumentKeyIDWithKey;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreAccess;
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

    public DocumentGuardStuff testCreateDocumentGuardForDocumentKeyIDWithKey(KeyStoreAccess keyStoreAccess, DocumentKeyIDWithKey documentKeyIDWithKey, ExtendedKeystorePersistence keystorePersistence) {
        try {
            KeyStoreService keyStoreService = new KeyStoreService(keystorePersistence);

            {
                KeyStore userKeyStore = keyStoreService.loadKeystore(keyStoreAccess.getKeyStoreLocation(), keyStoreAccess.getKeyStoreAuth().getUserpass());
                Assert.assertEquals("Number of entries of KeyStore is 15", 15, userKeyStore.size());
            }

            DocumentGuardService documentGuardService = new DocumentGuardService(keystorePersistence, guardExtendedPersistence);
            documentGuardService.createDocumentGuard(keyStoreAccess, documentKeyIDWithKey);
            System.out.println("documentKeyID:" + documentKeyIDWithKey.getDocumentKeyID());
            return new DocumentGuardStuff(documentGuardService, documentKeyIDWithKey.getDocumentKeyID());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public DocumentGuardStuff testCreateDocumentGuard(KeyStoreAccess keyStoreAccess, ExtendedKeystorePersistence keystorePersistence) {
        try {
            KeyStoreService keyStoreService = new KeyStoreService(keystorePersistence);

            {
                KeyStore userKeyStore = keyStoreService.loadKeystore(keyStoreAccess.getKeyStoreLocation(), keyStoreAccess.getKeyStoreAuth().getUserpass());
                Assert.assertEquals("Number of entries of KeyStore is 15", 15, userKeyStore.size());
            }

            DocumentGuardService documentGuardService = new DocumentGuardService(keystorePersistence, guardExtendedPersistence);
            DocumentKeyID documentKeyID = documentGuardService.createDocumentGuard(keyStoreAccess);
            System.out.println("documentKeyID:" + documentKeyID);
            return new DocumentGuardStuff(documentGuardService, documentKeyID);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public DocumentKeyIDWithKey testLoadDocumentGuard(
            KeyStoreAccess keyStoreAccess,
            ExtendedKeystorePersistence keystorePersistence,
            DocumentKeyID documentKeyID) {
        try {
            DocumentGuardService documentGuardService = new DocumentGuardService(keystorePersistence, guardExtendedPersistence);
            DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardService.loadDocumentKeyIDWithKeyFromDocumentGuard(keyStoreAccess, documentKeyID);
            System.out.println("key des Guards ist :" + documentKeyIDWithKey.getDocumentKey());
            System.out.println("LOAD DocumentKey:" + HexUtil.conventBytesToHexString(documentKeyIDWithKey.getDocumentKey().getSecretKey().getEncoded()));
            return documentKeyIDWithKey;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }

    public static class DocumentGuardStuff {
        public final DocumentGuardService documentGuardService;
        public final DocumentKeyID documentKeyID;

        public DocumentGuardStuff(DocumentGuardService documentGuardService, DocumentKeyID documentKeyID) {
            this.documentGuardService = documentGuardService;
            this.documentKeyID = documentKeyID;
        }
    }
}
