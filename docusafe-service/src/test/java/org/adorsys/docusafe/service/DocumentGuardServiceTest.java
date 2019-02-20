package org.adorsys.docusafe.service;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.utils.HexUtil;
import org.adorsys.docusafe.service.impl.DocumentGuardServiceImpl;
import org.adorsys.docusafe.service.impl.GuardKeyType;
import org.adorsys.docusafe.service.types.DocumentKeyID;
import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.encobject.domain.KeyStoreAccess;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.impl.BlobStoreKeystorePersistenceImpl;
import org.adorsys.encobject.types.OverwriteFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 02.01.18.
 */
public class DocumentGuardServiceTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentGuardServiceTest.class);
    private ExtendedStoreConnection extendedStoreConnection;

    public DocumentGuardServiceTest(ExtendedStoreConnection extendedStoreConnection) {
        this.extendedStoreConnection = extendedStoreConnection;
    }

    public DocumentGuardStuff testCreateAsymmetricDocumentGuardForDocumentKeyIDWithKey(KeyStoreAccess keyStoreAccess,
                                                                                       DocumentKeyIDWithKey documentKeyIDWithKey) {
        try {
            DocumentGuardService documentGuardService = new DocumentGuardServiceImpl(extendedStoreConnection);
            documentGuardService.createDocumentGuardFor(GuardKeyType.PUBLIC_KEY, keyStoreAccess, documentKeyIDWithKey, OverwriteFlag.FALSE);
            LOGGER.debug("documentKeyID:" + documentKeyIDWithKey.getDocumentKeyID());
            return new DocumentGuardStuff(documentGuardService, documentKeyIDWithKey);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public DocumentGuardStuff testCreateSymmetricDocumentGuard(KeyStoreAccess keyStoreAccess) {
        try {
            DocumentGuardService documentGuardService = new DocumentGuardServiceImpl(extendedStoreConnection);
            DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardService.createDocumentKeyIdWithKey();
            documentGuardService.createDocumentGuardFor(GuardKeyType.SECRET_KEY, keyStoreAccess, documentKeyIDWithKey, OverwriteFlag.FALSE);
            return new DocumentGuardStuff(documentGuardService, documentKeyIDWithKey);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public DocumentKeyIDWithKey testLoadDocumentGuard(
            KeyStoreAccess keyStoreAccess,
            DocumentKeyID documentKeyID) {
        try {
            DocumentGuardService documentGuardService = new DocumentGuardServiceImpl(extendedStoreConnection);
            DocumentKeyIDWithKey documentKeyIDWithKeyAndAccessType = documentGuardService.loadDocumentKeyIDWithKeyFromDocumentGuard(keyStoreAccess, documentKeyID);
            LOGGER.debug("key des Guards ist :" + documentKeyIDWithKeyAndAccessType.getDocumentKey());
            LOGGER.debug("LOAD DocumentKey:" + HexUtil.convertBytesToHexString(documentKeyIDWithKeyAndAccessType.getDocumentKey().getSecretKey().getEncoded()));
            return documentKeyIDWithKeyAndAccessType;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public DocumentKeyIDWithKey createKeyIDWithKey() {
        BlobStoreKeystorePersistenceImpl keystorePersistence = null;
        DocumentGuardService documentGuardService = new DocumentGuardServiceImpl(extendedStoreConnection);
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
