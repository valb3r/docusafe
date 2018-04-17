package org.adorsys.docusafe.business;

import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentStream;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.service.types.AccessType;

import com.nimbusds.jose.jwk.JWK;

/**
 * Created by peter on 19.01.18 at 16:30.
 */
public interface DocumentSafeService {
    /**
     * User
     */
    void createUser(UserIDAuth userIDAuth);
    void destroyUser(UserIDAuth userIDAuth);

    boolean userExists(UserID userID);
    
    /**
     * returns the user public encryption key of the given user.
     */
    JWK findPublicEncryptionKey(UserID userID);
    
    /**
     * Document
     */
    void storeDocument(UserIDAuth userIDAuth, DSDocument dsDocument);
    DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    void storeDocumentStream(UserIDAuth userIDAuth, DSDocumentStream dsDocumentStream);
    DSDocumentStream readDocumentStream(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    void deleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);
    boolean documentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN);
    void deleteFolder(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN);

    /**
     * Grants
     */
    void grantAccessToUserForFolder(UserIDAuth userIDAuth, UserID receiverUserID, DocumentDirectoryFQN documentDirectoryFQN, AccessType accessType);

    void storeGrantedDocument(UserIDAuth userIDAuth, UserID documentOwner, DSDocument dsDocument);
    DSDocument readGrantedDocument(UserIDAuth userIDAuth, UserID documentOwner, DocumentFQN documentFQN);
}
