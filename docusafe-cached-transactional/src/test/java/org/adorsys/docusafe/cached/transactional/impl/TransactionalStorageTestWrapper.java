package org.adorsys.docusafe.cached.transactional.impl;

import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.transactional.TransactionalFileStorage;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.types.ListRecursiveFlag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by peter on 09.07.18 at 14:40.
 */
public class TransactionalStorageTestWrapper implements TransactionalFileStorage {
    int MAX = 22;

    public static final String CREATE_USER = "createUser";
    public static final String DESTROY_USER = "destroyUser";
    public static final String USER_EXISTS = "userExists";
    public static final String GRANT_ACCESS = "grantAccess";
    public static final String STORE_DOCUMENT = "storeDocument";
    public static final String STORE_GRANTED_DOCUMENT = "storeGrantedDocument";
    public static final String READ_GRANTED_DOCUMENT = "readGrantedDocument";
    public static final String READ_DOCUMENT = "readDocument";
    public static final String DOCUMENT_EXISTS = "documentExists";
    public static final String GRANTED_DOCUMENT_EXISTS = "grantedDocumentExists";
    public static final String DELETE_DOCUMENT = "deleteDocument";
    public static final String LIST_DOCUMENTS = "listDocuments";
    public static final String BEGIN_TX = "beginTx";
    public static final String STORE_DOCUMENT_TX = "storeDocumentTx";
    public static final String READ_DOCUMENT_TX = "readDocumentTx";
    public static final String DELETE_DOCUMENT_TX = "deleteDocumentTx";
    public static final String LIST_DOCUMENTS_TX = "listDocumentsTx";
    public static final String DOCUMENT_EXISTS_TX = "documentExistsTx";
    public static final String DELETE_FOLDER_TX = "deleteFolderTx";
    public static final String END_TRANSACTION = "endTransaction";
    public Map<String, Integer> counterMap = new HashMap<>();

    private TransactionalFileStorage realTransactionalFileStorage;
    public TransactionalStorageTestWrapper(TransactionalFileStorage realTransactionalFileStorage) {
        this.realTransactionalFileStorage = realTransactionalFileStorage;
        inc(CREATE_USER);
        inc(DESTROY_USER);
        inc(USER_EXISTS);
        inc(GRANT_ACCESS);
        inc(STORE_DOCUMENT);
        inc(STORE_GRANTED_DOCUMENT);
        inc(READ_GRANTED_DOCUMENT);
        inc(READ_DOCUMENT);
        inc(DOCUMENT_EXISTS);
        inc(GRANTED_DOCUMENT_EXISTS);
        inc(DELETE_DOCUMENT);
        inc(LIST_DOCUMENTS);
        inc(BEGIN_TX);
        inc(STORE_DOCUMENT_TX);
        inc(READ_DOCUMENT_TX);
        inc(DELETE_DOCUMENT_TX);
        inc(LIST_DOCUMENTS_TX);
        inc(DELETE_FOLDER_TX);
        inc(END_TRANSACTION);
    }

    @Override
    public void createUser(UserIDAuth userIDAuth) {
        inc(CREATE_USER);
        realTransactionalFileStorage.createUser(userIDAuth);
    }

    @Override
    public void destroyUser(UserIDAuth userIDAuth) {
        inc(DESTROY_USER);
        realTransactionalFileStorage.destroyUser(userIDAuth);
    }

    @Override
    public boolean userExists(UserID userID) {
        inc(USER_EXISTS);
        return realTransactionalFileStorage.userExists(userID);
    }

    @Override
    public void grantAccess(UserIDAuth userIDAuth, UserID receiverUserID) {
        inc(GRANT_ACCESS);
        realTransactionalFileStorage.grantAccess(userIDAuth, receiverUserID);
    }

    @Override
    public void storeDocument(UserIDAuth userIDAuth, DSDocument dsDocument) {
        inc(STORE_DOCUMENT);
        realTransactionalFileStorage.storeDocument(userIDAuth, dsDocument);
    }

    @Override
    public void storeDocument(UserIDAuth userIDAuth, UserID documentOwner, DSDocument dsDocument) {
        inc(STORE_GRANTED_DOCUMENT);
        realTransactionalFileStorage.storeDocument(userIDAuth, documentOwner, dsDocument);
    }

    @Override
    public DSDocument readDocument(UserIDAuth userIDAuth, UserID documentOwner, DocumentFQN documentFQN) {
        inc(READ_GRANTED_DOCUMENT);
        return realTransactionalFileStorage.readDocument(userIDAuth, documentOwner, documentFQN);
    }

    @Override
    public boolean documentExists(UserIDAuth userIDAuth, UserID documentOwner, DocumentFQN documentFQN) {
        inc(GRANTED_DOCUMENT_EXISTS);
        return realTransactionalFileStorage.documentExists(userIDAuth, documentOwner, documentFQN);
    }

    @Override
    public DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        inc(READ_DOCUMENT);
        return realTransactionalFileStorage.readDocument(userIDAuth, documentFQN);
    }

    @Override
    public boolean documentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        inc(DOCUMENT_EXISTS);
        return realTransactionalFileStorage.documentExists(userIDAuth, documentFQN);
    }

    @Override
    public void deleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        inc(DELETE_DOCUMENT);
        realTransactionalFileStorage.deleteDocument(userIDAuth, documentFQN);
    }

    @Override
    public BucketContentFQN listDocuments(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        inc(LIST_DOCUMENTS);
        return realTransactionalFileStorage.listDocuments(userIDAuth, documentDirectoryFQN, recursiveFlag);
    }

    @Override
    public TxID beginTransaction(UserIDAuth userIDAuth) {
        inc(BEGIN_TX);
        return realTransactionalFileStorage.beginTransaction(userIDAuth);
    }

    @Override
    public void txStoreDocument(TxID txid, UserIDAuth userIDAuth, DSDocument dsDocument) {
        inc(STORE_DOCUMENT_TX);
        realTransactionalFileStorage.txStoreDocument(txid, userIDAuth, dsDocument);
    }

    @Override
    public DSDocument txReadDocument(TxID txid, UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        inc(READ_DOCUMENT_TX);
        return realTransactionalFileStorage.txReadDocument(txid, userIDAuth, documentFQN);
    }

    @Override
    public void txDeleteDocument(TxID txid, UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        inc(DELETE_DOCUMENT_TX);
        realTransactionalFileStorage.txDeleteDocument(txid, userIDAuth, documentFQN);
    }

    @Override
    public BucketContentFQN txListDocuments(TxID txid, UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        inc(LIST_DOCUMENTS_TX);
        return realTransactionalFileStorage.txListDocuments(txid, userIDAuth, documentDirectoryFQN, recursiveFlag);
    }

    @Override
    public boolean txDocumentExists(TxID txid, UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        inc(DOCUMENT_EXISTS_TX);
        return realTransactionalFileStorage.txDocumentExists(txid, userIDAuth, documentFQN);
    }

    @Override
    public void txDeleteFolder(TxID txid, UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN) {
        inc(DELETE_FOLDER_TX);
        realTransactionalFileStorage.txDeleteFolder(txid, userIDAuth, documentDirectoryFQN);
    }

    @Override
    public void endTransaction(TxID txid, UserIDAuth userIDAuth) {
        inc(END_TRANSACTION);
        realTransactionalFileStorage.endTransaction(txid, userIDAuth);
    }

    private void inc(String s) {
        if (!counterMap.containsKey(s)) {
            counterMap.put(s, new Integer(-1));
        }
        counterMap.put(s, counterMap.get(s) + 1);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("TransactionalStorageTestWrapper{");
        List<String> keys = new ArrayList<>();
        keys.addAll(counterMap.keySet());

        Collections.sort(keys);
        keys.forEach(key -> {
            sb.append("\n");
            sb.append(fill(MAX, key));
            sb.append(" -> ");
            sb.append(counterMap.get(key));
        });

        sb.append("\n}");
        return sb.toString();
    }

    private String fill(int m, String s) {
        while(s.length() < m) {
            s = s + " ";
        }
        return s;
    }
}
