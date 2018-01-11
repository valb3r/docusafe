package org.adorsys.documentsafe.layer02service;

import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;

/**
 * Created by peter on 11.01.18.
 */
public interface InterfaceDocumentGuardService {
    DocumentKeyIDWithKey createDocumentKeyIdWithKey();

    void createSymmetricDocumentGuard(KeyStoreAccess keyStoreAccess, DocumentKeyIDWithKey documentKeyIDWithKey);

    void createAsymmetricDocumentGuard(KeyStoreAccess receiverKeyStoreAccess, DocumentKeyIDWithKey documentKeyIDWithKey);

    DocumentKeyIDWithKey loadDocumentKeyIDWithKeyFromDocumentGuard(KeyStoreAccess keyStoreAccess, DocumentKeyID documentKeyID);
}
