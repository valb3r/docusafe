package org.adorsys.docusafe.cached.transactional.impl;

import org.adorsys.docusafe.business.impl.BucketContentFQNImpl;
import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.cached.transactional.exceptions.CacheException;
import org.adorsys.docusafe.transactional.TransactionalDocumentSafeService;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final static Logger LOGGER = LoggerFactory.getLogger(CachedTransactionalContext.class);
    private String id = UUID.randomUUID().toString() + " ";
    private Map<DocumentFQN, DSDocument> mapToStore = null;
    private Map<DocumentFQN, DSDocument> mapToRead = null;
    private Set<DocumentFQN> setToDelete = null;
    private TransactionalDocumentSafeService transactionalFileStorage = null;
    private BucketContentFQN bucketContent = null;


    public CachedTransactionalContext(TransactionalDocumentSafeService transactionalFileStorage) {
        this.transactionalFileStorage = transactionalFileStorage;
        mapToStore = new HashMap<>();
        mapToRead = new HashMap<>();
        setToDelete = new HashSet<>();
    }

    public void txStoreDocument(DSDocument dsDocument) {
        setToDelete.remove(dsDocument.getDocumentFQN());
        mapToStore.put(dsDocument.getDocumentFQN(), dsDocument);
        mapToRead.put(dsDocument.getDocumentFQN(), dsDocument);
    }

    public DSDocument txReadDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        if (mapToRead.containsKey(documentFQN)) {
            return mapToRead.get(documentFQN);
        }
        if (setToDelete.contains(documentFQN)) {
            throw new CacheException("document " + documentFQN + " has been deleted before. can not be read");
        }
        DSDocument dsDocument = transactionalFileStorage.txReadDocument(userIDAuth, documentFQN);
        mapToRead.put(dsDocument.getDocumentFQN(), dsDocument);
        return dsDocument;

    }

    public void txDeleteDocument(DocumentFQN documentFQN) {
        setToDelete.add(documentFQN);
        mapToStore.remove(documentFQN);
        mapToRead.remove(documentFQN);

    }

    public BucketContentFQN txListDocuments(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        if (bucketContent == null) {
            bucketContent = transactionalFileStorage.txListDocuments(userIDAuth, new DocumentDirectoryFQN("/"), ListRecursiveFlag.TRUE);
        }

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

    public boolean txDocumentExists(TxID txid, UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        if (setToDelete.contains(documentFQN)) {
            return false;
        }
        if (mapToStore.containsKey(documentFQN)) {
            return true;
        }
        return (transactionalFileStorage.txDocumentExists(userIDAuth, documentFQN));
    }

    public void endTransaction(final UserIDAuth userIDAuth) {
        setToDelete.forEach(documentFQN -> transactionalFileStorage.txDeleteDocument(userIDAuth, documentFQN));
        mapToStore.keySet().forEach(documentFQN ->  transactionalFileStorage.txStoreDocument(userIDAuth, mapToStore.get(documentFQN)));
        transactionalFileStorage.endTransaction(userIDAuth);
    }

    public void freeMemory() {
        int numWrite = mapToStore.size();
        int numRead = mapToRead.size();
        long sumWrite = mapToStore.values().stream().mapToLong(dsDocument -> dsDocument.getDocumentContent().getValue().length).sum();
        long sumRead = mapToRead.values().stream().mapToLong(dsDocument -> dsDocument.getDocumentContent().getValue().length).sum();
        LOGGER.debug("freeMemory("+numWrite + ":" + sumWrite+", "+ numRead + ":" + sumRead+")");
        mapToStore.clear();
        mapToRead.clear();
        setToDelete.clear();
        transactionalFileStorage = null;
        bucketContent = null;
    }
}
