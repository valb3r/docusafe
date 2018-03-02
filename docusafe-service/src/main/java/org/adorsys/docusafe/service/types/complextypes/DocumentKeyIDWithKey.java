package org.adorsys.docusafe.service.types.complextypes;

import org.adorsys.docusafe.service.types.DocumentKey;
import org.adorsys.docusafe.service.types.DocumentKeyID;

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

	@Override
	public String toString() {
		return "DocumentKeyIDWithKey{" +
				documentKeyID +
				", " + documentKey +
				'}';
	}
}
