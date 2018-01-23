package org.adorsys.documentsafe.layer02service.types.complextypes;

import org.adorsys.documentsafe.layer02service.types.DocumentContent;
import org.adorsys.encobject.domain.ContentMetaInfo;

/**
 * Created by peter on 23.01.18 at 18:41.
 */
public class DocumentContentWithContentMetaInfo {
    private DocumentContent documentContent;
    private ContentMetaInfo contentMetaInfo;

    public DocumentContentWithContentMetaInfo(DocumentContent documentContent, ContentMetaInfo contentMetaInfo) {
        this.documentContent = documentContent;
        this.contentMetaInfo = contentMetaInfo;
    }

    public DocumentContent getDocumentContent() {
        return documentContent;
    }

    public ContentMetaInfo getContentMetaInfo() {
        return contentMetaInfo;
    }
}
