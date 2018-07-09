package org.adorsys.docusafe.service.impl;

import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKeyAndAccessType;
import org.adorsys.encobject.domain.ReadKeyPassword;

/**
 * Created by peter on 28.06.18 at 14:12.
 */
public class PasswordAndDocumentKeyIDWithKeyAndAccessType {
    private ReadKeyPassword readKeyPassword;
    private DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType;

    public PasswordAndDocumentKeyIDWithKeyAndAccessType(ReadKeyPassword readKeyPassword, DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType) {
        this.readKeyPassword = readKeyPassword;
        this.documentKeyIDWithKeyAndAccessType = documentKeyIDWithKeyAndAccessType;
    }

    public ReadKeyPassword getReadKeyPassword() {
        return readKeyPassword;
    }

    public DocumentKeyIDWithKeyAndAccessType getDocumentKeyIDWithKeyAndAccessType() {
        return documentKeyIDWithKeyAndAccessType;
    }

    @Override
    public String toString() {
        return "PasswordAndDocumentKeyIDWithKeyAndAccessType{" +
                "readKeyPassword=" + readKeyPassword +
                ", documentKeyIDWithKeyAndAccessType=" + documentKeyIDWithKeyAndAccessType +
                '}';
    }
}
