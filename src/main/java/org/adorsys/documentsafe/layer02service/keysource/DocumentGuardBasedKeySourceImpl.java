package org.adorsys.documentsafe.layer02service.keysource;

import org.adorsys.documentsafe.layer00common.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer00common.basetypes.DocumentKeyID;
import org.adorsys.documentsafe.layer00common.basetypes.KeyID;
import org.adorsys.documentsafe.layer00common.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer00common.complextypes.KeyStoreAccess;
import org.adorsys.documentsafe.layer00common.complextypes.KeyStoreLocation;
import org.adorsys.documentsafe.layer02service.DocumentGuardService;
import org.adorsys.documentsafe.layer01persistence.keysource.KeySource;

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
			DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardService.loadDocumentKeyIDWithKeyFromDocumentGuard(keyStoreAccess, documentKeyID);
			return documentKeyIDWithKey.getDocumentKey().getSecretKey();
		} catch (Exception e) {
			throw BaseExceptionHandler.handle(e);
		}
	}

}
