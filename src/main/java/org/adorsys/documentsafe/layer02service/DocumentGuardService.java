package org.adorsys.documentsafe.layer02service;

import org.adorsys.documentsafe.layer02service.impl.GuardKeyType;
import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKeyAndAccessType;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.encobject.types.OverwriteFlag;

/**
 * Created by peter on 11.01.18.
 */
public interface DocumentGuardService {
    DocumentKeyIDWithKey createDocumentKeyIdWithKey();

    void createDocumentGuardFor(GuardKeyType guardKeyType, KeyStoreAccess keyStoreAccess,
                                DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType,
                                OverwriteFlag overwriteFlag);

    DocumentKeyIDWithKeyAndAccessType loadDocumentKeyIDWithKeyAndAccessTypeFromDocumentGuard(KeyStoreAccess keyStoreAccess,
                                                                                             DocumentKeyID documentKeyID);
}
