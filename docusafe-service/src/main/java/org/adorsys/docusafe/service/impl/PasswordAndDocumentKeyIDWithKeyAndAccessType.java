package org.adorsys.docusafe.service.impl;

import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.encobject.domain.ReadKeyPassword;

/**
 * Created by peter on 28.06.18 at 14:12.
 */
public class PasswordAndDocumentKeyIDWithKeyAndAccessType {
    private ReadKeyPassword readKeyPassword;
    private DocumentKeyIDWithKey documentKeyIDWithKey;

    public PasswordAndDocumentKeyIDWithKeyAndAccessType(ReadKeyPassword readKeyPassword, DocumentKeyIDWithKey documentKeyIDWithKey) {
        this.readKeyPassword = readKeyPassword;
        this.documentKeyIDWithKey = documentKeyIDWithKey;
    }

    public ReadKeyPassword getReadKeyPassword() {
        return readKeyPassword;
    }

    public DocumentKeyIDWithKey getDocumentKeyIDWithKey() {
        return documentKeyIDWithKey;
    }

    @Override
    public String toString() {
        return "PasswordAndDocumentKeyIDWithKey{" +
                "readKeyPassword=" + readKeyPassword +
                ", documentKeyIDWithKey=" + documentKeyIDWithKey +
                '}';
    }
}
