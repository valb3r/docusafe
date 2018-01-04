package org.adorsys.resource.server.persistence;

import java.security.Key;

import javax.security.auth.callback.CallbackHandler;

import org.adorsys.resource.server.basetypes.DocumentGuardName;
import org.adorsys.resource.server.basetypes.DocumentKeyID;
import org.adorsys.resource.server.basetypes.UserID;
import org.adorsys.resource.server.complextypes.DocumentGuard;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.basetypes.BucketName;
import org.adorsys.resource.server.persistence.basetypes.KeyID;
import org.adorsys.resource.server.service.DocumentGuardService;

public class DocumentGuardBasedKeySourceImpl implements KeySource {

    private DocumentGuardService documentGuardService;

    private UserID userID;
    private CallbackHandler userKeystoreHandler;
    private CallbackHandler keyPassHandler;
	private BucketName keysourceBucketName;
	private BucketName guardBucketName;

	public DocumentGuardBasedKeySourceImpl(DocumentGuardService documentGuardService,
			UserID userID, CallbackHandler userKeystoreHandler, CallbackHandler keyPassHandler,
			BucketName keysourceBucketName, BucketName guardBucketName) {
		super();
		this.documentGuardService = documentGuardService;
		this.userID = userID;
		this.userKeystoreHandler = userKeystoreHandler;
		this.keyPassHandler = keyPassHandler;
		this.keysourceBucketName = keysourceBucketName;
		this.guardBucketName = guardBucketName;
	}

	@Override
	public Key readKey(KeyID keyID) {
		
        // Load DokumentKeyID from guard.
        try {
			DocumentGuardName documentGuardName = new DocumentGuardName(guardBucketName, userID, new DocumentKeyID(keyID.getValue()));
			DocumentGuard documentGuard = documentGuardService.loadDocumentGuard(documentGuardName, keysourceBucketName, userKeystoreHandler, keyPassHandler);
			return documentGuard.getDocumentKey().getSecretKey();
		} catch (Exception e) {
			throw BaseExceptionHandler.handle(e);
		}
	}

}
