package org.adorsys.docusafe.service.impl;

import org.adorsys.docusafe.service.types.DocumentKeyID;
import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKeyAndAccessType;
import org.adorsys.encobject.domain.KeyStoreAccess;

/**
 * Created by peter on 13.07.18 at 13:26.
 */
public interface DocumentKeyID2DocumentKeyCache {
    DocumentKeyIDWithKeyAndAccessType get(KeyStoreAccess keyStoreAccess, DocumentKeyID documentKeyID);
}
