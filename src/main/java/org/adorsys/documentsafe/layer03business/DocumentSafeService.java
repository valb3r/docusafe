package org.adorsys.documentsafe.layer03business;

import org.adorsys.documentsafe.layer02service.types.DocumentContent;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentLocation;
import org.adorsys.documentsafe.layer03business.types.complex.UserIDAuth;

/**
 * Created by peter on 19.01.18 at 16:30.
 */
public interface DocumentSafeService {
    void createUserID(UserIDAuth userIDAuth);
    DocumentContent readDocument(UserIDAuth userIDAuth, DocumentLocation documentLocation);
}
