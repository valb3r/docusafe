package org.adorsys.docusafe.business;

import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentStream;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.service.types.AccessType;

/**
 * Created by peter on 19.01.18 at 16:30.
 */
public interface DocumentSafeService {
    void createUser(UserIDAuth userIDAuth);
    void destroyUser(UserIDAuth userIDAuth);

    void storeDocument(UserIDAuth userIDAuth, DSDocument dsDocument);
    DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    void storeDocumentStream(UserIDAuth userIDAuth, DSDocumentStream dsDocumentStream);

    void grantAccessToUserForFolder(UserIDAuth userIDAuth, UserID receiverUserID, DocumentDirectoryFQN documentDirectoryFQN, AccessType accessType);

    void storeDocument(UserIDAuth userIDAuth, UserID documentOwner, DSDocument dsDocument);
    DSDocument readDocument(UserIDAuth userIDAuth, UserID documentOwner, DocumentFQN documentFQN);

    void linkDocument(UserIDAuth userIDAuth, DocumentFQN sourceDocumentFQN, DocumentFQN destinationDocumentFQN);

}
