package org.adorsys.documentsafe.persistence.keysource;

import org.adorsys.documentsafe.exceptions.KeySourceException;
import org.adorsys.documentsafe.persistence.basetypes.KeyID;
import org.adorsys.documentsafe.persistence.complextypes.DocumentKeyIDWithKey;

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
            throw new KeySourceException("expected key id " + documentKeyIDWithKey.getDocumentKeyID() + " but got key id "+ keyID);
        }
        return documentKeyIDWithKey.getDocumentKey().getSecretKey();
    }
}
