package org.adorsys.docusafe.service;

import org.adorsys.docusafe.business.types.MemoryContext;
import org.adorsys.docusafe.service.impl.GuardKeyType;
import org.adorsys.docusafe.service.types.DocumentKeyID;
import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKeyAndAccessType;
import org.adorsys.encobject.domain.KeyStoreAccess;
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

    /**
     * Caching
     */
    void setMemoryContext(MemoryContext memoryContext);

}
