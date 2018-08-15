package org.adorsys.docusafe.transactional;

import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.encobject.types.ListRecursiveFlag;

/**
 * Created by peter on 15.08.18 at 11:55.
 */
public interface NonTransactionalDocumentSafeService {

    // NON-TRANSACTIONAL FOR OWNER
    void createUser(UserIDAuth userIDAuth);
    void destroyUser(UserIDAuth userIDAuth);
    boolean userExists(UserID userID);
    void grantAccessToNonTxFolder(UserIDAuth userIDAuth, UserID receiverUserID, DocumentDirectoryFQN documentDirectoryFQN);

    void nonTxStoreDocument(UserIDAuth userIDAuth, DSDocument dsDocument);
    DSDocument nonTxReadDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);
    boolean nonTxDocumentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN);
    void nonTxDeleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);
    BucketContentFQN nonTxListDocuments(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag);

    // NON-TRANSACTIONAL FOR OTHERS
    void nonTxStoreDocument(UserIDAuth userIDAuth, UserID documentOwner, DSDocument dsDocument);
    DSDocument nonTxReadDocument(UserIDAuth userIDAuth, UserID documentOwner, DocumentFQN documentFQN);
    boolean nonTxDocumentExists(UserIDAuth userIDAuth, UserID documentOwner, DocumentFQN documentFQN);
}
