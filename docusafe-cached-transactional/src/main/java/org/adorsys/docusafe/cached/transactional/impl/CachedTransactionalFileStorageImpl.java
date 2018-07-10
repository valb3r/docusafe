package org.adorsys.docusafe.cached.transactional.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.cached.transactional.CachedTransactionalFileStorage;
import org.adorsys.docusafe.cached.transactional.exceptions.CacheException;
import org.adorsys.docusafe.transactional.RequestMemoryContext;
import org.adorsys.docusafe.transactional.TransactionalFileStorage;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public static final String USER_CONTEXT = "USER_CONTEXT";
    private TransactionalFileStorage transactionalFileStorage;
    private RequestMemoryContext requestContext;

    public CachedTransactionalFileStorageImpl(RequestMemoryContext requestContext, TransactionalFileStorage transactionalFileStorage) {
        this.transactionalFileStorage = transactionalFileStorage;
        this.requestContext = requestContext;
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
    public void storeDocument(UserIDAuth userIDAuth, DSDocument dsDocument) {
        transactionalFileStorage.storeDocument(userIDAuth, dsDocument);
    }

    @Override
    public void storeDocument(UserIDAuth userIDAuth, UserID documentOwner, DSDocument dsDocument) {
        transactionalFileStorage.storeDocument(userIDAuth, documentOwner, dsDocument);

    }

    @Override
    public DSDocument readDocument(UserIDAuth userIDAuth, UserID documentOwner, DocumentFQN documentFQN) {
        return transactionalFileStorage.readDocument(userIDAuth, documentOwner, documentFQN);
    }

    @Override
    public DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        return transactionalFileStorage.readDocument(userIDAuth, documentFQN);
    }

    @Override
    public boolean documentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        return transactionalFileStorage.documentExists(userIDAuth, documentFQN);
    }

    @Override
    public boolean documentExists(UserIDAuth userIDAuth, UserID documentOwner, DocumentFQN documentFQN) {
        return transactionalFileStorage.documentExists(userIDAuth, documentOwner, documentFQN);
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
        createTransactionalContext().beginTransaction(userIDAuth);
    }

    @Override
    public void txStoreDocument(DSDocument dsDocument) {
        getTransactionalContext().txStoreDocument(dsDocument);
    }

    @Override
    public DSDocument txReadDocument(DocumentFQN documentFQN) {
        return getTransactionalContext().txReadDocument(documentFQN);
    }

    @Override
    public void txDeleteDocument(DocumentFQN documentFQN) {
        getTransactionalContext().txDeleteDocument(documentFQN);
    }

    @Override
    public BucketContentFQN txListDocuments(DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        return getTransactionalContext().txListDocuments(documentDirectoryFQN, recursiveFlag);
    }

    @Override
    public boolean txDocumentExists(DocumentFQN documentFQN) {
        return getTransactionalContext().txDocumentExists(documentFQN);
    }

    @Override
    public void txDeleteFolder(DocumentDirectoryFQN documentDirectoryFQN) {
        throw new BaseException("Who needs this interface");

    }

    @Override
    public void endTransaction() {
        getTransactionalContext().endTransaction();

    }

    private CachedTransactionalContext createTransactionalContext() {
        Object o = requestContext.get(USER_CONTEXT);
        if (o != null) {
            throw new CacheException("RequestContext has Transactional Object. New Transaction can not be started");
        }

        CachedTransactionalContext cachedTransactionalContext = new CachedTransactionalContext(transactionalFileStorage);
        requestContext.put(USER_CONTEXT, cachedTransactionalContext);
        return cachedTransactionalContext;
    }

    private CachedTransactionalContext getTransactionalContext() {
        CachedTransactionalContext cachedTransactionalContext = (CachedTransactionalContext) requestContext.get(USER_CONTEXT);
        if (cachedTransactionalContext == null) {
            throw new CacheException("RequestContext has no Transactional Object.");
        }
        return cachedTransactionalContext;
    }
}
