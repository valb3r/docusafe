package org.adorsys.documentsafe.layer03rest.types;

import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;

/**
 * Created by peter on 10.01.18.
 */
public class VersionInformation {
    private final String info;
    private final DocumentKeyID documentKeyID;

    public VersionInformation(String info, DocumentKeyID documentKeyID) {
        this.info = info;
        this.documentKeyID = documentKeyID;
    }

    public String getInfo() {
        return info;
    }

    public DocumentKeyID getDocumentKeyID() {
        return documentKeyID;
    }
}
