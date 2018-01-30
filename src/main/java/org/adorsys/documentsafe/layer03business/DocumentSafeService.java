package org.adorsys.documentsafe.layer03business;

import org.adorsys.documentsafe.layer03business.types.AccessType;
import org.adorsys.documentsafe.layer03business.types.UserID;
import org.adorsys.documentsafe.layer03business.types.complex.DSDocument;
import org.adorsys.documentsafe.layer03business.types.complex.DocumentDirectoryFQN;
import org.adorsys.documentsafe.layer03business.types.complex.DocumentFQN;
import org.adorsys.documentsafe.layer03business.types.complex.UserIDAuth;

/**
 * Created by peter on 19.01.18 at 16:30.
 */
public interface DocumentSafeService {
    void createUser(UserIDAuth userIDAuth);
    void destroyUser(UserIDAuth userIDAuth);

    void storeDocument(UserIDAuth userIDAuth, DSDocument dsDocument);
    DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    void grantAccessToUserForFolder(UserIDAuth userIDAuth, UserID receiverUserID, DocumentDirectoryFQN documentDirectoryFQN, AccessType accessType);
    DSDocument readDocument(UserIDAuth userIDAuth, UserID documentOwner, DocumentFQN documentFQN);

    void linkDocument(UserIDAuth userIDAuth, DocumentFQN sourceDocumentFQN, DocumentFQN destinationDocumentFQN);

}
