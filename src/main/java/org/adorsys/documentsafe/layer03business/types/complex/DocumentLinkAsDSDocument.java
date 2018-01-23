package org.adorsys.documentsafe.layer03business.types.complex;

import org.adorsys.documentsafe.layer02service.types.DocumentContent;

/**
 * Created by peter on 23.01.18 at 18:24.
 */
public class DocumentLinkAsDSDocument extends DSDocument {
    public DocumentLinkAsDSDocument(DocumentFQN documentFQN, DocumentContent documentContent) {
        super(documentFQN, documentContent, null);
    }
}
