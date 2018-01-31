package org.adorsys.documentsafe.layer02service.keysource;

import org.adorsys.documentsafe.layer02service.DocumentGuardService;
import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.encobject.complextypes.KeyStoreLocation;
import org.adorsys.encobject.keysource.KeySource;
import org.adorsys.encobject.types.KeyID;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;

public class DocumentGuardBasedKeySourceImpl implements KeySource {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentGuardBasedKeySourceImpl.class);
    private DocumentGuardService documentGuardService;

    private KeyStoreAccess keyStoreAccess;
    private KeyStoreLocation keyStoreLocation;

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
            DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardService.loadDocumentKeyIDWithKeyFromDocumentGuard(keyStoreAccess, documentKeyID);
            LOGGER.debug("LOADED DOCUMENT KEY WITH ID " + documentKeyIDWithKey);
            return documentKeyIDWithKey.getDocumentKey().getSecretKey();
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

}
