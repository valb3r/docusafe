package org.adorsys.resource.server.persistence;

import org.adorsys.resource.server.exceptions.BaseException;
import org.adorsys.resource.server.persistence.basetypes.KeyID;
import org.adorsys.resource.server.persistence.complextypes.DocumentKeyIDWithKey;

import java.security.Key;

/**
 * Created by peter on 08.01.18.
 */
public class DocumentKeyIDWithKeyBasedSourceImpl implements KeySource {
    private final DocumentKeyIDWithKey documentKeyIDWithKey;

    public DocumentKeyIDWithKeyBasedSourceImpl(DocumentKeyIDWithKey documentKeyIDWithKey) {
        this.documentKeyIDWithKey = documentKeyIDWithKey;
    }

    @Override
    public Key readKey(KeyID keyID) {
        if (keyID.equals(documentKeyIDWithKey.getDocumentKeyID())) {
            throw new BaseException("expected key id " + documentKeyIDWithKey.getDocumentKeyID() + " but got key id "+ keyID);
        }
        return documentKeyIDWithKey.getDocumentKey().getSecretKey();
    }
}
