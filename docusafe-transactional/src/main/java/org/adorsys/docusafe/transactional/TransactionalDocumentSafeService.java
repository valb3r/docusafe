package org.adorsys.docusafe.transactional;

import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.types.ListRecursiveFlag;

/**
 * Created by peter on 11.06.18 at 14:56.
 */
public interface TransactionalDocumentSafeService extends NonTransactionalDocumentSafeService {
    // TRANSACTIONAL
    void beginTransaction(UserIDAuth userIDAuth);

    void txStoreDocument(UserIDAuth userIDAuth, DSDocument dsDocument);
    DSDocument txReadDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);
    void txDeleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);
    BucketContentFQN txListDocuments(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag);
    boolean txDocumentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN);
    void txDeleteFolder(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN);

    void endTransaction(UserIDAuth userIDAuth);

}
