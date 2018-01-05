package org.adorsys.resource.server.persistence;

import java.security.Key;

import javax.security.auth.callback.CallbackHandler;

import org.adorsys.resource.server.basetypes.DocumentGuardName;
import org.adorsys.resource.server.basetypes.DocumentKeyID;
import org.adorsys.resource.server.complextypes.DocumentGuard;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.basetypes.KeyID;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreName;
import org.adorsys.resource.server.service.DocumentGuardService;

public class DocumentGuardBasedKeySourceImpl implements KeySource {

    private DocumentGuardService documentGuardService;

    private CallbackHandler userKeystoreHandler;
    private CallbackHandler keyPassHandler;
	private KeyStoreName keyStoreName;

	public DocumentGuardBasedKeySourceImpl(DocumentGuardService documentGuardService,KeyStoreName keyStoreName,
			CallbackHandler userKeystoreHandler, CallbackHandler keyPassHandler) {
		super();
		this.documentGuardService = documentGuardService;
		this.userKeystoreHandler = userKeystoreHandler;
		this.keyPassHandler = keyPassHandler;
		this.keyStoreName = keyStoreName;
	}

	@Override
	public Key readKey(KeyID keyID) {
		
        // Load DokumentKeyID from guard.
        try {
        	// We assume keystore container is docuement guard container 
        	DocumentGuardName documentGuardName =  new DocumentGuardName(keyStoreName, new DocumentKeyID(keyID.getValue()));
			DocumentGuard documentGuard = documentGuardService.loadDocumentGuard(documentGuardName, userKeystoreHandler, keyPassHandler);
			return documentGuard.getDocumentKey().getSecretKey();
		} catch (Exception e) {
			throw BaseExceptionHandler.handle(e);
		}
	}

}
