package org.adorsys.docusafe.transactional;

import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.transactional.types.TxID;

/**
 * Created by peter on 11.06.18 at 14:56.
 */
public interface FileStorage {
    TxID beginTransaction(UserIDAuth userIDAuth);

    void storeDocument(TxID txid, UserIDAuth userIDAuth, DSDocument dsDocument);
    DSDocument readDocument(TxID txid, UserIDAuth userIDAuth, DocumentFQN documentFQN);
    void deleteDocument(TxID txid, UserIDAuth userIDAuth, DocumentFQN documentFQN);
    boolean documentExists(TxID txid, UserIDAuth userIDAuth, DocumentFQN documentFQN);
    void deleteFolder(TxID txid, UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN);

    void endTransaction(TxID txid, UserIDAuth userIDAuth);

}
