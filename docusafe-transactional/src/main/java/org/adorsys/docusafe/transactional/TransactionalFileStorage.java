package org.adorsys.docusafe.transactional;

import org.adorsys.docusafe.business.types.UserID;
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
public interface TransactionalFileStorage extends NonTransactionalFileStorage {
    // TRANSACTIONAL
    TxID beginTransaction(UserIDAuth userIDAuth);

    void txStoreDocument(TxID txid, UserIDAuth userIDAuth, DSDocument dsDocument);
    DSDocument txReadDocument(TxID txid, UserIDAuth userIDAuth, DocumentFQN documentFQN);
    void txDeleteDocument(TxID txid, UserIDAuth userIDAuth, DocumentFQN documentFQN);
    BucketContentFQN txListDocuments(TxID txid, UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag);
    boolean txDocumentExists(TxID txid, UserIDAuth userIDAuth, DocumentFQN documentFQN);
    void txDeleteFolder(TxID txid, UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN);

    void endTransaction(TxID txid, UserIDAuth userIDAuth);

}
