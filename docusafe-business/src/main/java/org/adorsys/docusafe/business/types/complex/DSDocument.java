package org.adorsys.docusafe.business.types.complex;

import org.adorsys.docusafe.service.types.DocumentContent;

/**
 * Created by peter on 22.01.18 at 08:14.
 * DocumentSafeDocument -> DSDocument
 */
public class DSDocument {
    private DocumentFQN documentFQN;
    private DocumentContent documentContent;
    private DSDocumentMetaInfo dsDocumentMetaInfo;

    public DSDocument(DocumentFQN documentFQN, DocumentContent documentContent, DSDocumentMetaInfo dsDocumentMetaInfo) {
        this.documentFQN = documentFQN;
        this.documentContent = documentContent;
        this.dsDocumentMetaInfo = dsDocumentMetaInfo;
    }

    public DocumentFQN getDocumentFQN() {
        return documentFQN;
    }

    public DocumentContent getDocumentContent() {
        return documentContent;
    }

    public DSDocumentMetaInfo getDsDocumentMetaInfo() {
        return dsDocumentMetaInfo;
    }
}
