package org.adorsys.resource.server.persistence;

import org.adorsys.resource.server.persistence.complextypes.DocumentGuardLocation;
import org.adorsys.resource.server.basetypes.DocumentKeyID;
import org.adorsys.resource.server.complextypes.DocumentGuard;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.basetypes.KeyID;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreAuth;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreLocation;
import org.adorsys.resource.server.service.DocumentGuardService;

import java.security.Key;

public class DocumentGuardBasedKeySourceImpl implements KeySource {

    private DocumentGuardService documentGuardService;

	private KeyStoreAuth keyStoreAuth;
	private KeyStoreLocation keyStoreLocation;

	public DocumentGuardBasedKeySourceImpl(DocumentGuardService documentGuardService, KeyStoreLocation keyStoreLocation,
										   KeyStoreAuth keyStoreAuth) {
		super();
		this.documentGuardService = documentGuardService;
		this.keyStoreAuth = keyStoreAuth;
		this.keyStoreLocation = keyStoreLocation;
	}

	@Override
	public Key readKey(KeyID keyID) {
		
        // Load DokumentKeyID from guard.
        try {
        	// We assume keystore container is docuement guard container 
        	DocumentGuardLocation documentGuardLocation =  new DocumentGuardLocation(keyStoreLocation, new DocumentKeyID(keyID.getValue()));
			DocumentGuard documentGuard = documentGuardService.loadDocumentGuard(documentGuardLocation, keyStoreAuth);
			return documentGuard.getDocumentKey().getSecretKey();
		} catch (Exception e) {
			throw BaseExceptionHandler.handle(e);
		}
	}

}
