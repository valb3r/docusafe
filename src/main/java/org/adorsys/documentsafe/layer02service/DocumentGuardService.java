package org.adorsys.documentsafe.layer02service;

import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKeyAndAccessType;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;

/**
 * Created by peter on 11.01.18.
 */
public interface DocumentGuardService {
    DocumentKeyIDWithKey createDocumentKeyIdWithKey();

    void createSymmetricDocumentGuard(KeyStoreAccess keyStoreAccess,
                                      DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType);

    void createAsymmetricDocumentGuard(KeyStoreAccess receiverKeyStoreAccess,
                                       DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType);

    DocumentKeyIDWithKeyAndAccessType loadDocumentKeyIDWithKeyAndAccessTypeFromDocumentGuard(KeyStoreAccess keyStoreAccess,
                                                                                             DocumentKeyID documentKeyID);
}
