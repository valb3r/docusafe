package org.adorsys.documentsafe.service;

import org.adorsys.documentsafe.layer00common.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer00common.utils.HexUtil;
import org.adorsys.documentsafe.layer01persistence.ExtendedBlobStoreConnection;
import org.adorsys.documentsafe.layer01persistence.ExtendedKeystorePersistence;
import org.adorsys.documentsafe.layer01persistence.ExtendedObjectPersistence;
import org.adorsys.documentsafe.layer02service.InterfaceDocumentGuardService;
import org.adorsys.documentsafe.layer02service.impl.DocumentGuardService;
import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.utils.TestFsBlobStoreFactory;
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
        guardContainerPersistence = new ContainerPersistence(new ExtendedBlobStoreConnection(guardContextFactory));
        guardExtendedPersistence = new ExtendedObjectPersistence(new ExtendedBlobStoreConnection(guardContextFactory));

    }
    public static void afterClass() {

    }

    public DocumentGuardStuff testCreateAsymmetricDocumentGuardForDocumentKeyIDWithKey(KeyStoreAccess keyStoreAccess, DocumentKeyIDWithKey documentKeyIDWithKey, ExtendedKeystorePersistence keystorePersistence) {
        try {
            InterfaceDocumentGuardService documentGuardService = new DocumentGuardService(keystorePersistence, guardExtendedPersistence);
            documentGuardService.createAsymmetricDocumentGuard(keyStoreAccess, documentKeyIDWithKey);
            LOGGER.info("documentKeyID:" + documentKeyIDWithKey.getDocumentKeyID());
            return new DocumentGuardStuff(documentGuardService, documentKeyIDWithKey);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public DocumentGuardStuff testCreateSymmetricDocumentGuard(KeyStoreAccess keyStoreAccess, ExtendedKeystorePersistence keystorePersistence) {
        try {
            InterfaceDocumentGuardService documentGuardService = new DocumentGuardService(keystorePersistence, guardExtendedPersistence);
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
            InterfaceDocumentGuardService documentGuardService = new DocumentGuardService(keystorePersistence, guardExtendedPersistence);
            DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardService.loadDocumentKeyIDWithKeyFromDocumentGuard(keyStoreAccess, documentKeyID);
            LOGGER.info("key des Guards ist :" + documentKeyIDWithKey.getDocumentKey());
            LOGGER.info("LOAD DocumentKey:" + HexUtil.conventBytesToHexString(documentKeyIDWithKey.getDocumentKey().getSecretKey().getEncoded()));
            return documentKeyIDWithKey;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public DocumentKeyIDWithKey createKeyIDWithKey() {
        ExtendedKeystorePersistence keystorePersistence = null;
        InterfaceDocumentGuardService documentGuardService = new DocumentGuardService(keystorePersistence, guardExtendedPersistence);
        return documentGuardService.createDocumentKeyIdWithKey();
    }

    public static class DocumentGuardStuff {
        public final InterfaceDocumentGuardService documentGuardService;
        public final DocumentKeyIDWithKey documentKeyIDWithKey;

        public DocumentGuardStuff(InterfaceDocumentGuardService documentGuardService, DocumentKeyIDWithKey documentKeyIDWithKey) {
            this.documentGuardService = documentGuardService;
            this.documentKeyIDWithKey = documentKeyIDWithKey;
        }
    }
}
