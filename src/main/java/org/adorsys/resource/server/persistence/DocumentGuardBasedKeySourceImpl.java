package org.adorsys.resource.server.persistence;

import org.adorsys.resource.server.basetypes.BucketName;
import org.adorsys.resource.server.basetypes.DocumentGuardName;
import org.adorsys.resource.server.basetypes.DocumnentKeyID;
import org.adorsys.resource.server.basetypes.UserID;
import org.adorsys.resource.server.complextypes.DocumentGuard;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.service.DocumentGuardService;

import javax.security.auth.callback.CallbackHandler;
import java.security.Key;

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
			DocumentGuardName documentGuardName = new DocumentGuardName(userID, new DocumnentKeyID(keyID.getValue()));
			DocumentGuard documentGuard = documentGuardService.loadDocumentGuard(documentGuardName, keysourceBucketName, guardBucketName, userKeystoreHandler, keyPassHandler);
			return documentGuard.getDocumentKey().getSecretKey();
		} catch (Exception e) {
			throw BaseExceptionHandler.handle(e);
		}
	}

}
