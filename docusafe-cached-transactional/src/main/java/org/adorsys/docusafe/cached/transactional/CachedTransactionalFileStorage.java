package org.adorsys.docusafe.cached.transactional;

import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.types.ListRecursiveFlag;

/**
 * Created by peter on 21.06.18 at 11:49.
 */
public interface CachedTransactionalFileStorage {

    // NON-TRANSACTIONAL and NON-CACHED
    void createUser(UserIDAuth userIDAuth);
    void destroyUser(UserIDAuth userIDAuth);
    boolean userExists(UserID userID);
    void grantAccess(UserIDAuth userIDAuth, UserID receiverUserID);
    void storeDocument(UserIDAuth userIDAuth, UserID documentOwner, DSDocument dsDocument);
    DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);
    void deleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);
    BucketContentFQN listDocuments(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag);

    // TRANSACTIONAL and CACHED
    void beginTransaction(UserIDAuth userIDAuth);
    void txStoreDocument(DSDocument dsDocument);
    DSDocument txReadDocument(DocumentFQN documentFQN);
    void txDeleteDocument(DocumentFQN documentFQN);
    BucketContentFQN txListDocuments(DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag);
    boolean txDocumentExists(DocumentFQN documentFQN);
    void txDeleteFolder(DocumentDirectoryFQN documentDirectoryFQN);
    void endTransaction();
}


