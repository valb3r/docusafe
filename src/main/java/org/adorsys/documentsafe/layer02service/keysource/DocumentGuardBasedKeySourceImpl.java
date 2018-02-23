package org.adorsys.documentsafe.layer02service.keysource;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer02service.DocumentGuardService;
import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKeyAndAccessType;
import org.adorsys.encobject.domain.KeyStoreAccess;
import org.adorsys.encobject.keysource.KeySource;
import org.adorsys.encobject.types.KeyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;

public class DocumentGuardBasedKeySourceImpl implements KeySource {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentGuardBasedKeySourceImpl.class);
    private DocumentGuardService documentGuardService;

    private KeyStoreAccess keyStoreAccess;

    public DocumentGuardBasedKeySourceImpl(DocumentGuardService documentGuardService, KeyStoreAccess keyStoreAccess) {
        super();
        this.documentGuardService = documentGuardService;
        this.keyStoreAccess = keyStoreAccess;
    }

    @Override
    public Key readKey(KeyID keyID) {

        // Load DokumentKeyID from guard.
        try {
            // We assume keystore container is docuement guard container
            DocumentKeyID documentKeyID = new DocumentKeyID(keyID.getValue());
            LOGGER.debug("try to load KEY WITH ID " + documentKeyID);
            DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType = documentGuardService.loadDocumentKeyIDWithKeyAndAccessTypeFromDocumentGuard(keyStoreAccess, documentKeyID);
            LOGGER.debug("LOADED DOCUMENT KEY WITH ID " + documentKeyIDWithKeyAndAccessType);
            return documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey().getDocumentKey().getSecretKey();
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

}
