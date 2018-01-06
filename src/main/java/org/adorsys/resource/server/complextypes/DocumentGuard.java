package org.adorsys.resource.server.complextypes;

import org.adorsys.resource.server.persistence.complextypes.DocumentGuardLocation;
import org.adorsys.resource.server.basetypes.DocumentKey;

/**
 * Created by peter on 23.12.17 at 18:33.
 * 
 * THe technical JWE-Implementation happens at the storage layer.
 */
public class DocumentGuard {
    DocumentGuardLocation documentGuardLocation;
    DocumentKey documentKey;

    public DocumentGuard(DocumentGuardLocation documentGuardLocation, DocumentKey documentKey) {
		super();
		this.documentGuardLocation = documentGuardLocation;
		this.documentKey = documentKey;
	}

	public DocumentGuardLocation getDocumentGuardLocation() {
        return documentGuardLocation;
    }

	public DocumentKey getDocumentKey() {
		return documentKey;
	}
    
}
