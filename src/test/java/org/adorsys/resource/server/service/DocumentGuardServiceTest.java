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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 02.01.18.
 */
public class DocumentGuardServiceTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentGuardServiceTest.class);

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

    public DocumentGuardStuff testCreateAsymmetricDocumentGuardForDocumentKeyIDWithKey(KeyStoreAccess keyStoreAccess, DocumentKeyIDWithKey documentKeyIDWithKey, ExtendedKeystorePersistence keystorePersistence) {
        try {
            DocumentGuardService documentGuardService = new DocumentGuardService(keystorePersistence, guardExtendedPersistence);
            documentGuardService.createAsymmetricDocumentGuard(keyStoreAccess, documentKeyIDWithKey);
            LOGGER.info("documentKeyID:" + documentKeyIDWithKey.getDocumentKeyID());
            return new DocumentGuardStuff(documentGuardService, documentKeyIDWithKey);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public DocumentGuardStuff testCreateSymmetricDocumentGuard(KeyStoreAccess keyStoreAccess, ExtendedKeystorePersistence keystorePersistence) {
        try {
            DocumentGuardService documentGuardService = new DocumentGuardService(keystorePersistence, guardExtendedPersistence);
            DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardService.createDocumentKeyIdWithKey();
            documentGuardService.createSymmetricDocumentGuard(keyStoreAccess, documentKeyIDWithKey);
            return new DocumentGuardStuff(documentGuardService, documentKeyIDWithKey);
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
            LOGGER.info("key des Guards ist :" + documentKeyIDWithKey.getDocumentKey());
            LOGGER.info("LOAD DocumentKey:" + HexUtil.conventBytesToHexString(documentKeyIDWithKey.getDocumentKey().getSecretKey().getEncoded()));
            return documentKeyIDWithKey;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }

    public static class DocumentGuardStuff {
        public final DocumentGuardService documentGuardService;
        public final DocumentKeyIDWithKey documentKeyIDWithKey;

        public DocumentGuardStuff(DocumentGuardService documentGuardService, DocumentKeyIDWithKey documentKeyIDWithKey) {
            this.documentGuardService = documentGuardService;
            this.documentKeyIDWithKey = documentKeyIDWithKey;
        }
    }
}
