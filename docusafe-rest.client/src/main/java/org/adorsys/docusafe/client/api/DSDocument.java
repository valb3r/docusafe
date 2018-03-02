package org.adorsys.docusafe.client.api;

/**
 * Created by peter on 27.02.18 at 12:48.
 */
public class DSDocument {
    String documentFQN;
    String documentContent;
    DocumentMetaInfo dsDocumentMetaInfo;

    public static class DocumentMetaInfo {
        long size;

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        @Override
        public String toString() {
            return "{" +
                    "size=" + size +
                    '}';
        }
    }

    public String getDocumentFQN() {
        return documentFQN;
    }

    public void setDocumentFQN(String documentFQN) {
        this.documentFQN = documentFQN;
    }

    public String getDocumentContent() {
        return documentContent;
    }

    public void setDocumentContent(String documentContent) {
        this.documentContent = documentContent;
    }

    public DocumentMetaInfo getDsDocumentMetaInfo() {
        return dsDocumentMetaInfo;
    }

    public void setDsDocumentMetaInfo(DocumentMetaInfo dsDocumentMetaInfo) {
        this.dsDocumentMetaInfo = dsDocumentMetaInfo;
    }

    @Override
    public String toString() {
        return "DSDocument{" +
                "documentFQN='" + documentFQN + '\'' +
                ", hexBinaryContent='" + documentContent + '\'' +
                ", metaInfo=" + dsDocumentMetaInfo +
                '}';
    }
}