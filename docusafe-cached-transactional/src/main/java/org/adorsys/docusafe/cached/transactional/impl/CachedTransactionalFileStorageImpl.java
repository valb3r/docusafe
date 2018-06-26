package org.adorsys.docusafe.cached.transactional.impl;

import org.adorsys.docusafe.business.impl.BucketContentFQNImpl;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.cached.transactional.CachedTransactionalFileStorage;
import org.adorsys.docusafe.cached.transactional.exceptions.CacheException;
import org.adorsys.docusafe.service.types.BucketContent;
import org.adorsys.docusafe.transactional.TransactionalFileStorage;
import org.adorsys.docusafe.transactional.impl.TransactionalFileStorageImpl;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

/**
 * Created by peter on 21.06.18 at 11:51.
 *
 * Es gibt drei Listen:
 * mapToStore enthält alle Dokumente, die gespeichert werden sollen.
 * Es wird nicht gepüft, ob sich der Inhalt gehändert hat, oder nicht.
 *
 * mapToRead enthält alle Documente, die gelesen wurden. Wenn diese anschliessend
 * gespeichert werden, dann sind sie zusätzlich in mapToStore.
 *
 * setToDelete enhält alle Namen der Dokumente, die gelöscht werden sollen.
 * Der name darf dann nicht in mapToRead oder mapToStore auftauchen.
 */
public class CachedTransactionalFileStorageImpl implements CachedTransactionalFileStorage {
    private final static Logger LOGGER = LoggerFactory.getLogger(CachedTransactionalFileStorageImpl.class);
    private TransactionalFileStorage transactionalFileStorage;
    private String id = UUID.randomUUID().toString() + " ";
    private TxID txid = null;
    private UserIDAuth userIDAuth = null;
    private Map<DocumentFQN, DSDocument> mapToStore;
    private Map<DocumentFQN, DSDocument> mapToRead;
    private Set<DocumentFQN> setToDelete;
    private int totalReadsSaved;
    private int totalWritesSaved;
    private BucketContentFQN bucketContent;

    public CachedTransactionalFileStorageImpl(ExtendedStoreConnection extendedStoreConnection) {
        LOGGER.debug(id + "create");
        transactionalFileStorage = new TransactionalFileStorageImpl(extendedStoreConnection);
    }

    @Override
    public void createUser(UserIDAuth userIDAuth) {
        transactionalFileStorage.createUser(userIDAuth);
    }

    @Override
    public void destroyUser(UserIDAuth userIDAuth) {
        transactionalFileStorage.destroyUser(userIDAuth);
    }

    @Override
    public boolean userExists(UserID userID) {
        return transactionalFileStorage.userExists(userID);
    }

    @Override
    public void grantAccess(UserIDAuth userIDAuth, UserID receiverUserID) {
        transactionalFileStorage.grantAccess(userIDAuth, receiverUserID);
    }

    @Override
    public void storeDocument(UserIDAuth userIDAuth, UserID documentOwner, DSDocument dsDocument) {
        transactionalFileStorage.storeDocument(userIDAuth, documentOwner, dsDocument);

    }

    @Override
    public DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        return transactionalFileStorage.readDocument(userIDAuth, documentFQN);
    }

    @Override
    public void deleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        transactionalFileStorage.deleteDocument(userIDAuth, documentFQN);
    }

    @Override
    public BucketContentFQN listDocuments(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        return transactionalFileStorage.listDocuments(userIDAuth, documentDirectoryFQN, recursiveFlag);
    }

    @Override
    public void beginTransaction(UserIDAuth userIDAuth) {
        if (txid != null) {
            throw new CacheException(id + " beginTransaction not allowed due to pending tx " + txid);
        }
        this.userIDAuth = userIDAuth;
        txid = transactionalFileStorage.beginTransaction(userIDAuth);
        initTx();
    }

    @Override
    public void txStoreDocument(DSDocument dsDocument) {
        setToDelete.remove(dsDocument.getDocumentFQN());
        mapToStore.put(dsDocument.getDocumentFQN(), dsDocument);
        mapToRead.put(dsDocument.getDocumentFQN(), dsDocument);
        totalWritesSaved++;
    }

    @Override
    public DSDocument txReadDocument(DocumentFQN documentFQN) {
        if (mapToRead.containsKey(documentFQN)) {
            totalReadsSaved++;
            return mapToRead.get(documentFQN);
        }
        if (setToDelete.contains(documentFQN)) {
            throw new CacheException("document " + documentFQN + " has been deleted before. can not be read");
        }
        if (! bucketContent.getFiles().contains(documentFQN)) {
            throw new CacheException("document " + documentFQN + " does not exist");
        }
        DSDocument dsDocument = transactionalFileStorage.readDocument(txid, userIDAuth, documentFQN);
        mapToRead.put(dsDocument.getDocumentFQN(), dsDocument);
        return dsDocument;
    }

    @Override
    public void txDeleteDocument(DocumentFQN documentFQN) {
        setToDelete.add(documentFQN);
        mapToStore.remove(documentFQN);
        mapToRead.remove(documentFQN);
    }

    @Override
    public BucketContentFQN txListDocuments(DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        BucketContentFQN ret = new BucketContentFQNImpl();
        bucketContent.getFiles().forEach( file -> {
            if (file.getValue().startsWith(documentDirectoryFQN.getValue())) {
                if ( recursiveFlag.equals(ListRecursiveFlag.TRUE)) {
                    ret.getFiles().add(file);
                } else {
                    String fileWithoutRoot = file.getValue().substring(documentDirectoryFQN.getValue().length());
                    if (fileWithoutRoot.lastIndexOf(BucketPath.BUCKET_SEPARATOR) == 0) {
                        ret.getFiles().add(file);
                    }
                }
            }
        });
        bucketContent.getDirectories().forEach( dir -> {
            if (dir.getValue().startsWith(documentDirectoryFQN.getValue())) {
                if ( recursiveFlag.equals(ListRecursiveFlag.TRUE)) {
                    ret.getDirectories().add(dir);
                } else {
                    String dirWithoutRoot = dir.getValue().substring(documentDirectoryFQN.getValue().length());
                    if (dirWithoutRoot.lastIndexOf(BucketPath.BUCKET_SEPARATOR) == 0) {
                        ret.getDirectories().add(dir);
                    }
                }
            }
        });
        bucketContent.getFiles().removeAll(setToDelete);

        return null;


    }

    @Override
    public boolean txDocumentExists(DocumentFQN documentFQN) {
        return false;
    }

    @Override
    public void txDeleteFolder(DocumentDirectoryFQN documentDirectoryFQN) {

    }

    @Override
    public void endTransaction() {

    }

    private void initTx() {
        mapToStore = new HashMap<>();
        mapToRead = new HashMap<>();
        setToDelete = new HashSet<>();
        totalReadsSaved = 0;
        totalWritesSaved = 0;
        bucketContent = transactionalFileStorage.listDocuments(txid, userIDAuth, new DocumentDirectoryFQN("/"), ListRecursiveFlag.TRUE);
    }

    private void checkRunningTx() {
        if (txid == null) {
            throw new CacheException(id + " no transaction running");
        }

    }

}
