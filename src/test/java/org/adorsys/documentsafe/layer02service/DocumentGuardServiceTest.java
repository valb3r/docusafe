package org.adorsys.documentsafe.layer02service;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.utils.HexUtil;
import org.adorsys.documentsafe.layer02service.impl.DocumentGuardServiceImpl;
import org.adorsys.documentsafe.layer02service.impl.GuardKeyType;
import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKeyAndAccessType;
import org.adorsys.documentsafe.layer03business.types.AccessType;
import org.adorsys.encobject.domain.KeyStoreAccess;
import org.adorsys.encobject.service.BlobStoreKeystorePersistence;
import org.adorsys.encobject.service.ExtendedStoreConnection;
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
                                                                                       DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType) {
        try {
            DocumentGuardService documentGuardService = new DocumentGuardServiceImpl(extendedStoreConnection);
            documentGuardService.createDocumentGuardFor(GuardKeyType.PUBLIC_KEY, keyStoreAccess, documentKeyIDWithKeyAndAccessType, OverwriteFlag.FALSE);
            LOGGER.debug("documentKeyID:" + documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKeyID());
            return new DocumentGuardStuff(documentGuardService, documentKeyIDWithKeyAndAccessType);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public DocumentGuardStuff testCreateSymmetricDocumentGuard(KeyStoreAccess keyStoreAccess, AccessType accessType) {
        try {
            DocumentGuardService documentGuardService = new DocumentGuardServiceImpl(extendedStoreConnection);
            DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardService.createDocumentKeyIdWithKey();
            DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = new DocumentKeyIDWithKeyAndAccessType(documentKeyIDWithKey, accessType);
            documentGuardService.createDocumentGuardFor(GuardKeyType.SECRET_KEY, keyStoreAccess, documentKeyIDWithKeyAndAccessType, OverwriteFlag.FALSE);
            return new DocumentGuardStuff(documentGuardService, documentKeyIDWithKeyAndAccessType);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public DocumentKeyIDWithKeyAndAccessType testLoadDocumentGuard(
            KeyStoreAccess keyStoreAccess,
            DocumentKeyID documentKeyID) {
        try {
            DocumentGuardService documentGuardService = new DocumentGuardServiceImpl(extendedStoreConnection);
            DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = documentGuardService.loadDocumentKeyIDWithKeyAndAccessTypeFromDocumentGuard(keyStoreAccess, documentKeyID);
            LOGGER.debug("key des Guards ist :" + documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKey());
            LOGGER.debug("LOAD DocumentKey:" + HexUtil.convertBytesToHexString(documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKey().getSecretKey().getEncoded()));
            return documentKeyIDWithKeyAndAccessType;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public DocumentKeyIDWithKey createKeyIDWithKey() {
        BlobStoreKeystorePersistence keystorePersistence = null;
        DocumentGuardService documentGuardService = new DocumentGuardServiceImpl(extendedStoreConnection);
        return documentGuardService.createDocumentKeyIdWithKey();
    }

    public static class DocumentGuardStuff {
        public final DocumentGuardService documentGuardService;
        public final DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType;

        public DocumentGuardStuff(DocumentGuardService documentGuardService, DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType) {
            this.documentGuardService = documentGuardService;
            this.documentKeyIDWithKeyAndAccessType = documentKeyIDWithKeyAndAccessType;
        }
    }
}
