package org.adorsys.docusafe.transactional;

import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.transactional.types.TxBucketContentFQN;
import org.adorsys.docusafe.transactional.types.TxDocumentFQNVersion;
import org.adorsys.encobject.types.ListRecursiveFlag;

/**
 * Created by peter on 11.06.18 at 14:56.
 */
public interface TransactionalDocumentSafeService extends NonTransactionalDocumentSafeService {
    // TRANSACTIONAL
    void beginTransaction(UserIDAuth userIDAuth);

    /**
     * writes byted to a file and stores the given Metadata with that file
     *
     * @param userIDAuth user and password
     * @param dsDocument data and metadata and FQN to be stored
     */
    void txStoreDocument(UserIDAuth userIDAuth, DSDocument dsDocument);

    /**
     * Reads the content of a document
     *
     * @param userIDAuth user and password
     * @param documentFQN file to be read
     * @return an Object containing the metadata and the data
     */
    DSDocument txReadDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    /**
     * Deletes a single file
     *
     * @param userIDAuth user and password
     * @param documentFQN file to be deleted
     */
    void txDeleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    /**
     * Seeks all Files and Folders starting from a given root.
     *
     * @param userIDAuth user and password
     * @param documentDirectoryFQN folder to list
     * @param recursiveFlag flag, to search deep or not
     * @return an Object, that contains a list for all files and a list for all folders that are below the documentDirectoryFQN
     */
    TxBucketContentFQN txListDocuments(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag);

    /**
     * returns the Version number of the Document or throws a FileNotFoundException
     *
     * @param userIDAuth user and password
     * @param documentFQN the document, the version is requested for
     * @return the Version number
     */
    TxDocumentFQNVersion getVersion(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    /**
     * Checks the existance of a file
     *
     * @param userIDAuth user and password
     * @param documentFQN name of the file
     * @return true, if the file exists, false otherwise
     */
    boolean txDocumentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN);

    /**
     * All files in this folder und folders below this folder will be deleted
     * Actually folders do not exist unless they contain a file or folder that eventually contains a file
     *
     * @param userIDAuth user and password
     * @param documentDirectoryFQN name of the directory
     */
    void txDeleteFolder(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN);

    /**
     * Gets a file from the nonTx Context and stores it in the tx Context.
     * After this call, the file exists in both spaces (tx and nonTx).
     * After the commit, this file will be deleted automaticly.
     *
     * @param userIDAuth user and password
     * @param nonTxFQN name of the file in the non transactional context (e.g. source)
     * @param txFQN name of the file in the transactional context (e.g. destination)
     */
    void transferFromNonTxToTx(UserIDAuth userIDAuth, DocumentFQN nonTxFQN, DocumentFQN txFQN);

    /**
     * Commits the running transaction and all its changes.
     * If during this transaction any files have been transfered from the nonTxContext, they will automaticly be destroyed after
     * the commit. This "postcommit" action can not throw Exceptions.
     *
     * @param userIDAuth user and password
     */
    void endTransaction(UserIDAuth userIDAuth);
}
