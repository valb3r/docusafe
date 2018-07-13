package org.adorsys.docusafe.service.keysource;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.docusafe.service.DocumentGuardService;
import org.adorsys.docusafe.service.impl.DocumentKeyID2DocumentKeyCache;
import org.adorsys.docusafe.service.types.DocumentKeyID;
import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKeyAndAccessType;
import org.adorsys.encobject.domain.KeyStoreAccess;
import org.adorsys.encobject.service.api.KeySource;
import org.adorsys.encobject.types.KeyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;

public class DocumentGuardBasedKeySourceImpl implements KeySource {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentGuardBasedKeySourceImpl.class);
    private DocumentGuardService documentGuardService;
    private DocumentKeyID2DocumentKeyCache documentKeyID2DocumentKeyCache;

    private KeyStoreAccess keyStoreAccess;

    public DocumentGuardBasedKeySourceImpl(DocumentGuardService documentGuardService,
                                           KeyStoreAccess keyStoreAccess,
                                           DocumentKeyID2DocumentKeyCache documentKeyID2DocumentKeyCache) {
        super();
        this.documentGuardService = documentGuardService;
        this.keyStoreAccess = keyStoreAccess;
        this.documentKeyID2DocumentKeyCache = documentKeyID2DocumentKeyCache;
    }

    @Override
    public Key readKey(KeyID keyID) {

        // Load DokumentKeyID from guard.
        try {
            // We assume keystore container is docuement guard container
            DocumentKeyID documentKeyID = new DocumentKeyID(keyID.getValue());
            if (documentKeyID2DocumentKeyCache != null) {
                DocumentKeyIDWithKeyAndAccessType fromCache = documentKeyID2DocumentKeyCache.get(keyStoreAccess, documentKeyID);
                if (fromCache != null) {
                    LOGGER.debug("return cached KEY WITH ID " + documentKeyID);
                    return fromCache.getDocumentKeyIDWithKey().getDocumentKey().getSecretKey();
                }
            }
            LOGGER.debug("try to load KEY WITH ID " + documentKeyID);
            DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = documentGuardService.loadDocumentKeyIDWithKeyAndAccessTypeFromDocumentGuard(keyStoreAccess, documentKeyID);
            LOGGER.debug("LOADED DOCUMENT KEY WITH ID " + documentKeyIDWithKeyAndAccessType);
            return documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKey().getSecretKey();
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

}
