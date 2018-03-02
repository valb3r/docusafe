package org.adorsys.docusafe.service.types.complextypes;


import org.adorsys.docusafe.service.types.AccessType;

/**
 * Created by peter on 02.02.18 at 10:27.
 */
public class DocumentKeyIDWithKeyAndAccessType {
    private DocumentKeyIDWithKey documentKeyIDWithKey;
    private AccessType accessType;

    public DocumentKeyIDWithKeyAndAccessType(DocumentKeyIDWithKey documentKeyIDWithKey, AccessType accessType) {
        this.documentKeyIDWithKey = documentKeyIDWithKey;
        this.accessType = accessType;
    }

    public DocumentKeyIDWithKey getDocumentKeyIDWithKey() {
        return documentKeyIDWithKey;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    @Override
    public String toString() {
        return "DocumentKeyIDWithKeyAndAccessType{" +
                "documentKeyIDWithKey=" + documentKeyIDWithKey +
                ", accessType=" + accessType +
                '}';
    }
}
