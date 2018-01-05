package org.adorsys.resource.server.persistence;

import org.adorsys.resource.server.basetypes.DocumentGuardName;
import org.adorsys.resource.server.basetypes.DocumentKeyID;
import org.adorsys.resource.server.complextypes.DocumentGuard;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.basetypes.KeyID;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreAuth;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreName;
import org.adorsys.resource.server.service.DocumentGuardService;

import java.security.Key;

public class DocumentGuardBasedKeySourceImpl implements KeySource {

    private DocumentGuardService documentGuardService;

	private KeyStoreAuth keyStoreAuth;
	private KeyStoreName keyStoreName;

	public DocumentGuardBasedKeySourceImpl(DocumentGuardService documentGuardService, KeyStoreName keyStoreName,
										   KeyStoreAuth keyStoreAuth) {
		super();
		this.documentGuardService = documentGuardService;
		this.keyStoreAuth = keyStoreAuth;
		this.keyStoreName = keyStoreName;
	}

	@Override
	public Key readKey(KeyID keyID) {
		
        // Load DokumentKeyID from guard.
        try {
        	// We assume keystore container is docuement guard container 
        	DocumentGuardName documentGuardName =  new DocumentGuardName(keyStoreName, new DocumentKeyID(keyID.getValue()));
			DocumentGuard documentGuard = documentGuardService.loadDocumentGuard(documentGuardName, keyStoreAuth);
			return documentGuard.getDocumentKey().getSecretKey();
		} catch (Exception e) {
			throw BaseExceptionHandler.handle(e);
		}
	}

}
