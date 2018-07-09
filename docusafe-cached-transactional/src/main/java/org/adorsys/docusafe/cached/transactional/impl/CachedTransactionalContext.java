package org.adorsys.docusafe.cached.transactional.impl;

import org.adorsys.docusafe.business.impl.BucketContentFQNImpl;
import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.cached.transactional.exceptions.CacheException;
import org.adorsys.docusafe.transactional.TransactionalFileStorage;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.types.ListRecursiveFlag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by peter on 09.07.18 at 11:59.
 */
class CachedTransactionalContext {
    private String id = UUID.randomUUID().toString() + " ";
    private TxID txid = null;
    private UserIDAuth userIDAuth = null;
    private Map<DocumentFQN, DSDocument> mapToStore = null;
    private Map<DocumentFQN, DSDocument> mapToRead = null;
    private Set<DocumentFQN> setToDelete = null;
    private TransactionalFileStorage transactionalFileStorage = null;


    public CachedTransactionalContext(TransactionalFileStorage transactionalFileStorage) {
        this.transactionalFileStorage = transactionalFileStorage;
    }

    public void beginTransaction(UserIDAuth userIDAuth) {
        this.userIDAuth = userIDAuth;
        if (txid != null) {
            throw new CacheException("A transaction is running " + txid);
        }
        txid = transactionalFileStorage.beginTransaction(userIDAuth);
        mapToStore = new HashMap<>();
        mapToRead = new HashMap<>();
        setToDelete = new HashSet<>();
    }


    public void txStoreDocument(DSDocument dsDocument) {
        assertTxRunning();
        setToDelete.remove(dsDocument.getDocumentFQN());
        mapToStore.put(dsDocument.getDocumentFQN(), dsDocument);
        mapToRead.put(dsDocument.getDocumentFQN(), dsDocument);
    }

    public DSDocument txReadDocument(DocumentFQN documentFQN) {
        assertTxRunning();
        if (mapToRead.containsKey(documentFQN)) {
            return mapToRead.get(documentFQN);
        }
        if (setToDelete.contains(documentFQN)) {
            throw new CacheException("document " + documentFQN + " has been deleted before. can not be read");
        }
        DSDocument dsDocument = transactionalFileStorage.readDocument(txid, userIDAuth, documentFQN);
        mapToRead.put(dsDocument.getDocumentFQN(), dsDocument);
        return dsDocument;

    }

    public void txDeleteDocument(DocumentFQN documentFQN) {
        assertTxRunning();
        setToDelete.add(documentFQN);
        mapToStore.remove(documentFQN);
        mapToRead.remove(documentFQN);

    }

    public BucketContentFQN txListDocuments(DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        assertTxRunning();
        BucketContentFQN bucketContent = transactionalFileStorage.listDocuments(txid, userIDAuth, new DocumentDirectoryFQN("/"), ListRecursiveFlag.TRUE);

        BucketContentFQN ret = new BucketContentFQNImpl();
        // bucketContent ist die Liste aller Documente zu Beginn der Transaktion
        // Zunächst werden alle Documente rausgefiltert, die nicht auf den Pfad passen
        bucketContent.getFiles().forEach(file -> {
            if (file.getValue().startsWith(documentDirectoryFQN.getValue())) {
                if (recursiveFlag.equals(ListRecursiveFlag.TRUE)) {
                    ret.getFiles().add(file);
                } else {
                    String fileWithoutRoot = file.getValue().substring(documentDirectoryFQN.getValue().length());
                    if (fileWithoutRoot.lastIndexOf(BucketPath.BUCKET_SEPARATOR) == 0) {
                        ret.getFiles().add(file);
                    }
                }
            }
        });
        // Nun werden alle Directories rausgefiltert, die nicht auf den Pfad passen
        bucketContent.getDirectories().forEach(dir -> {
            if (dir.getValue().startsWith(documentDirectoryFQN.getValue())) {
                if (recursiveFlag.equals(ListRecursiveFlag.TRUE)) {
                    ret.getDirectories().add(dir);
                } else {
                    String dirWithoutRoot = dir.getValue().substring(documentDirectoryFQN.getValue().length());
                    if (dirWithoutRoot.lastIndexOf(BucketPath.BUCKET_SEPARATOR) == 0) {
                        ret.getDirectories().add(dir);
                    }
                }
            }
        });

        // Nun werden alle rausgefiltert, die bereits gelöscht wurden
        ret.getFiles().removeAll(setToDelete);

        // Nun werden alle neuen/oder erneuerten Documente hinzugefuegt, die auf den Pfad passen
        mapToStore.keySet().forEach(documentFQN -> {
                    if (documentFQN.getDocumentDirectory().getValue().startsWith(documentDirectoryFQN.getValue())) {
                        ret.getFiles().add(documentFQN);
                    }
                }
        );

        // Nun werden Duplicate rausgeworfen (Dokument wurde überschrieben, war also bereits bekannt)
        Set<DocumentFQN> allFiles = new HashSet<>(ret.getFiles());
        ret.getFiles().clear();
        ret.getFiles().addAll(new ArrayList<>(allFiles));

        return ret;
    }

    public boolean txDocumentExists(DocumentFQN documentFQN) {
        assertTxRunning();
        if (setToDelete.contains(documentFQN)) {
            return false;
        }
        if (mapToStore.containsKey(documentFQN)) {
            return true;
        }
        return (transactionalFileStorage.documentExists(txid, userIDAuth, documentFQN));
    }

    public void endTransaction() {
        setToDelete.forEach(documentFQN -> transactionalFileStorage.deleteDocument(txid, userIDAuth, documentFQN));
        mapToStore.keySet().forEach(documentFQN ->  transactionalFileStorage.storeDocument(txid, userIDAuth, mapToStore.get(documentFQN)));
        transactionalFileStorage.endTransaction(txid, userIDAuth);
        txid = null;
        userIDAuth = null;
    }

    private void assertTxRunning() {
        if (txid == null) {
            throw new CacheException("No Transactin is running yet");
        }

    }

}
