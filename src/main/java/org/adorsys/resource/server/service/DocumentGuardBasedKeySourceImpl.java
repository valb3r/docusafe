package org.adorsys.resource.server.service;

import java.security.Key;

import javax.security.auth.callback.CallbackHandler;

import org.adorsys.resource.server.basetypes.BucketName;
import org.adorsys.resource.server.basetypes.DocumentGuardName;
import org.adorsys.resource.server.basetypes.DocumnentKeyID;
import org.adorsys.resource.server.basetypes.UserID;
import org.adorsys.resource.server.complextypes.DocumentGuard;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.KeyID;
import org.adorsys.resource.server.persistence.KeySource;

public class DocumentGuardBasedKeySourceImpl implements KeySource {

    private DocumentGuardService documentGuardService;

    private UserID userID;
    private CallbackHandler userKeystoreHandler;
    private CallbackHandler keyPassHandler;
    private BucketName bucketName;
    
	public DocumentGuardBasedKeySourceImpl(DocumentGuardService documentGuardService,
			UserID userID, CallbackHandler userKeystoreHandler, CallbackHandler keyPassHandler,
			BucketName bucketName) {
		super();
		this.documentGuardService = documentGuardService;
		this.userID = userID;
		this.userKeystoreHandler = userKeystoreHandler;
		this.keyPassHandler = keyPassHandler;
		this.bucketName = bucketName;
	}

	@Override
	public Key readKey(KeyID keyID) {
		
        // Load DokumentKeyID from guard.
        try {
			DocumentGuardName documentGuardName = new DocumentGuardName(userID, new DocumnentKeyID(keyID.getValue()));
			DocumentGuard documentGuard = documentGuardService.loadDocumentGuard(documentGuardName, bucketName, userKeystoreHandler, keyPassHandler);
			return documentGuard.getDocumentKey().getSecretKey();
		} catch (Exception e) {
			throw BaseExceptionHandler.handle(e);
		}
	}

}
