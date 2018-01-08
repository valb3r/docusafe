package org.adorsys.resource.server.persistence;

import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.basetypes.DocumentKeyID;
import org.adorsys.resource.server.persistence.basetypes.KeyID;
import org.adorsys.resource.server.persistence.complextypes.DocumentGuard;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreAccess;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreLocation;
import org.adorsys.resource.server.service.DocumentGuardService;

import java.security.Key;

public class DocumentGuardBasedKeySourceImpl implements KeySource {

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
			DocumentGuard documentGuard = documentGuardService.loadDocumentGuard(keyStoreAccess, documentKeyID);
			return documentGuard.getDocumentKey().getSecretKey();
		} catch (Exception e) {
			throw BaseExceptionHandler.handle(e);
		}
	}

}
