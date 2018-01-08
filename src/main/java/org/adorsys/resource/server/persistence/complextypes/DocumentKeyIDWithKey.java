package org.adorsys.resource.server.persistence.complextypes;

import org.adorsys.resource.server.persistence.basetypes.DocumentKey;
import org.adorsys.resource.server.persistence.basetypes.DocumentKeyID;

/**
 * Created by peter on 23.12.17 at 18:33.
 * 
 * THe technical JWE-Implementation happens at the storage layer.
 */
public class DocumentKeyIDWithKey {
    private final DocumentKeyID documentKeyID;
    private final DocumentKey documentKey;

	public DocumentKeyIDWithKey(DocumentKeyID documentKeyID, DocumentKey documentKey) {
		this.documentKeyID = documentKeyID;
		this.documentKey = documentKey;
	}

	public DocumentKey getDocumentKey() {
		return documentKey;
	}

	public DocumentKeyID getDocumentKeyID() {

		return documentKeyID;
	}
}
