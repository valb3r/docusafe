package org.adorsys.resource.server.complextypes;

import org.adorsys.resource.server.basetypes.DocumentGuardName;
import org.adorsys.resource.server.basetypes.DocumentKey;

/**
 * Created by peter on 23.12.17 at 18:33.
 * 
 * THe technical JWE-Implementation happens at the storage layer.
 */
public class DocumentGuard {
    DocumentGuardName documentGuardName;
    DocumentKey documentKey;

    public DocumentGuard(DocumentGuardName documentGuardName, DocumentKey documentKey) {
		super();
		this.documentGuardName = documentGuardName;
		this.documentKey = documentKey;
	}

	public DocumentGuardName getDocumentGuardName() {
        return documentGuardName;
    }

	public DocumentKey getDocumentKey() {
		return documentKey;
	}
    
}
