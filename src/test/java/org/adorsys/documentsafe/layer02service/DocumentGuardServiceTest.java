package org.adorsys.documentsafe.layer02service;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.utils.HexUtil;
import org.adorsys.documentsafe.layer02service.impl.DocumentGuardServiceImpl;
import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.adorsys.encobject.service.BlobStoreKeystorePersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 02.01.18.
 */
public class DocumentGuardServiceTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentGuardServiceTest.class);
    private BlobStoreContextFactory factory;

    public DocumentGuardServiceTest(BlobStoreContextFactory factory) {
        this.factory = factory;
    }

    public DocumentGuardStuff testCreateAsymmetricDocumentGuardForDocumentKeyIDWithKey(KeyStoreAccess keyStoreAccess, DocumentKeyIDWithKey documentKeyIDWithKey) {
        try {
            DocumentGuardService documentGuardService = new DocumentGuardServiceImpl(factory);
            documentGuardService.createAsymmetricDocumentGuard(keyStoreAccess, documentKeyIDWithKey);
            LOGGER.debug("documentKeyID:" + documentKeyIDWithKey.getDocumentKeyID());
            return new DocumentGuardStuff(documentGuardService, documentKeyIDWithKey);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public DocumentGuardStuff testCreateSymmetricDocumentGuard(KeyStoreAccess keyStoreAccess) {
        try {
            DocumentGuardService documentGuardService = new DocumentGuardServiceImpl(factory);
            DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardService.createDocumentKeyIdWithKey();
            documentGuardService.createSymmetricDocumentGuard(keyStoreAccess, documentKeyIDWithKey);
            return new DocumentGuardStuff(documentGuardService, documentKeyIDWithKey);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public DocumentKeyIDWithKey testLoadDocumentGuard(
            KeyStoreAccess keyStoreAccess,
            DocumentKeyID documentKeyID) {
        try {
            DocumentGuardService documentGuardService = new DocumentGuardServiceImpl(factory);
            DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardService.loadDocumentKeyIDWithKeyFromDocumentGuard(keyStoreAccess, documentKeyID);
            LOGGER.debug("key des Guards ist :" + documentKeyIDWithKey.getDocumentKey());
            LOGGER.debug("LOAD DocumentKey:" + HexUtil.conventBytesToHexString(documentKeyIDWithKey.getDocumentKey().getSecretKey().getEncoded()));
            return documentKeyIDWithKey;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public DocumentKeyIDWithKey createKeyIDWithKey() {
        BlobStoreKeystorePersistence keystorePersistence = null;
        DocumentGuardService documentGuardService = new DocumentGuardServiceImpl(factory);
        return documentGuardService.createDocumentKeyIdWithKey();
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
