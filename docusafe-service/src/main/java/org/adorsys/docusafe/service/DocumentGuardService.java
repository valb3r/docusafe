package org.adorsys.docusafe.service;

import org.adorsys.docusafe.service.impl.GuardKeyType;
import org.adorsys.docusafe.service.types.DocumentKeyID;
import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.encobject.domain.KeyStoreAccess;
import org.adorsys.encobject.types.OverwriteFlag;

/**
 * Created by peter on 11.01.18.
 */
public interface DocumentGuardService {
    DocumentKeyIDWithKey createDocumentKeyIdWithKey();

    void createDocumentGuardFor(GuardKeyType guardKeyType, KeyStoreAccess keyStoreAccess,
                                DocumentKeyIDWithKey documentKeyIDWithKey,
                                OverwriteFlag overwriteFlag);

    DocumentKeyIDWithKey loadDocumentKeyIDWithKeyFromDocumentGuard(KeyStoreAccess keyStoreAccess,
                                                                                             DocumentKeyID documentKeyID);
}
