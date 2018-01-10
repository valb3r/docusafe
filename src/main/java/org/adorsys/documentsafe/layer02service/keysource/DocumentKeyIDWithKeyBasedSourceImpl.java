package org.adorsys.documentsafe.layer02service.keysource;

import org.adorsys.documentsafe.layer00common.exceptions.KeySourceException;
import org.adorsys.documentsafe.layer00common.basetypes.KeyID;
import org.adorsys.documentsafe.layer00common.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer01persistence.keysource.KeySource;

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