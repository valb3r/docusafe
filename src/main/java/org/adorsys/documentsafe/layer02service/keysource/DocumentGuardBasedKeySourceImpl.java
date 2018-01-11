package org.adorsys.documentsafe.layer02service.keysource;

import org.adorsys.documentsafe.layer00common.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer02service.InterfaceDocumentGuardService;
import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer01persistence.types.KeyID;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.KeyStoreLocation;
import org.adorsys.documentsafe.layer01persistence.keysource.KeySource;

import java.security.Key;

public class DocumentGuardBasedKeySourceImpl implements KeySource {

    private InterfaceDocumentGuardService documentGuardService;

	private KeyStoreAccess keyStoreAccess;
	private KeyStoreLocation keyStoreLocation;

	public DocumentGuardBasedKeySourceImpl(InterfaceDocumentGuardService documentGuardService, KeyStoreAccess keyStoreAccess) {
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
			DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardService.loadDocumentKeyIDWithKeyFromDocumentGuard(keyStoreAccess, documentKeyID);
			return documentKeyIDWithKey.getDocumentKey().getSecretKey();
		} catch (Exception e) {
			throw BaseExceptionHandler.handle(e);
		}
	}

}
