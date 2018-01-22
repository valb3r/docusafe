package org.adorsys.documentsafe.layer03business;

import org.adorsys.documentsafe.layer03business.types.complex.DocumentFQN;
import org.adorsys.documentsafe.layer03business.types.complex.DSDocument;
import org.adorsys.documentsafe.layer03business.types.complex.UserIDAuth;

/**
 * Created by peter on 19.01.18 at 16:30.
 */
public interface DocumentSafeService {
    void createUser(UserIDAuth userIDAuth);
    void storeDocument(UserIDAuth userIDAuth, DSDocument dsDocument);
    void destroyUser(UserIDAuth userIDAuth);
    DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);
}
