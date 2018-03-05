package org.adorsys.docusafe.business.types.complex;

import java.io.InputStream;

/**
 * Created by peter on 05.03.18 at 08:18.
 */
public class DSDocumentStream {
    private DocumentFQN documentFQN;
    private InputStream documentStream;
    private DSDocumentMetaInfo dsDocumentMetaInfo;

    public DSDocumentStream(DocumentFQN documentFQN, InputStream documentStream) {
        this.documentFQN = documentFQN;
        this.documentStream = documentStream;
        this.dsDocumentMetaInfo = new DSDocumentMetaInfo();
    }

    public DocumentFQN getDocumentFQN() {
        return documentFQN;
    }

    public InputStream getDocumentStream() {
        return documentStream;
    }

    public DSDocumentMetaInfo getDsDocumentMetaInfo() {
        return dsDocumentMetaInfo;
    }
}
