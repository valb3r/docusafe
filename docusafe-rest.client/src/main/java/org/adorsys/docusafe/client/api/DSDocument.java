package org.adorsys.docusafe.client.api;

import java.util.HashMap;

/**
 * Created by peter on 27.02.18 at 12:48.
 */
public class DSDocument {
    String documentFQN;
    String documentContent;
    DocumentMetaInfo dsDocumentMetaInfo;

    public static class DocumentMetaInfo {
        HashMap<String, String> map;
        public DocumentMetaInfo() {
            map = new HashMap<>();
        }

        public HashMap<String, String> getMap() {
            return map;
        }

        public void setMap(HashMap<String, String> map) {
            this.map = map;
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
