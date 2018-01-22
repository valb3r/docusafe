package org.adorsys.documentsafe.layer03business.types.complex;

import org.adorsys.documentsafe.layer02service.types.DocumentContent;

/**
 * Created by peter on 22.01.18 at 08:14.
 * DocumentSafeDocument -> DSDocument
 */
public class DSDocument {
    private DocumentFQN documentFQN;
    private DocumentContent documentContent;
    // TODO add some Meta Info, but not as Map<String,String> but fixed fields

    public DSDocument(DocumentFQN documentFQN, DocumentContent documentContent) {
        this.documentFQN = documentFQN;
        this.documentContent = documentContent;
    }

    public DocumentFQN getDocumentFQN() {
        return documentFQN;
    }

    public DocumentContent getDocumentContent() {
        return documentContent;
    }
}
