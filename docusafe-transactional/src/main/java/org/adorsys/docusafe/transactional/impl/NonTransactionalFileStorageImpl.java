package org.adorsys.docusafe.transactional.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.impl.BucketContentFQNImpl;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.service.types.AccessType;
import org.adorsys.docusafe.transactional.NonTransactionalFileStorage;
import org.adorsys.docusafe.transactional.RequestMemoryContext;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 15.08.18 at 11:57.
 */
public class NonTransactionalFileStorageImpl implements NonTransactionalFileStorage {
    private final static Logger LOGGER = LoggerFactory.getLogger(NonTransactionalFileStorageImpl.class);
    protected DocumentSafeService documentSafeService;
    protected RequestMemoryContext requestMemoryContext;
    final static DocumentDirectoryFQN nonTxContent = new DocumentDirectoryFQN("nonttx");

    public NonTransactionalFileStorageImpl(RequestMemoryContext requestMemoryContext, DocumentSafeService documentSafeService) {
        LOGGER.debug("new Instance of TransactionalFileStorageImpl");
        this.documentSafeService = documentSafeService;
        this.requestMemoryContext = requestMemoryContext;
    }
    // ============================================================================================
    // NON-TRANSACTIONAL FOR OWNER
    // ============================================================================================
    @Override
    public void createUser(UserIDAuth userIDAuth) {
        documentSafeService.createUser(userIDAuth);
    }

    @Override
    public void destroyUser(UserIDAuth userIDAuth) {
        documentSafeService.destroyUser(userIDAuth);
    }

    @Override
    public boolean userExists(UserID userID) {
        return documentSafeService.userExists(userID);
    }

    @Override
    public void grantAccessToNonTxFolder(UserIDAuth userIDAuth, UserID receiverUserID, DocumentDirectoryFQN documentDirectoryFQN) {
        LOGGER.debug("grant write access from " + userIDAuth.getUserID() + " to " + receiverUserID + " for " + nonTxContent.addDirectory(documentDirectoryFQN));
        documentSafeService.grantAccessToUserForFolder(userIDAuth, receiverUserID, nonTxContent.addDirectory(documentDirectoryFQN), AccessType.WRITE);
    }

    @Override
    public void nonTxStoreDocument(UserIDAuth userIDAuth, DSDocument dsDocument) {
        LOGGER.debug("nonTxStoreDocument " + dsDocument.getDocumentFQN() + " from folder " + nonTxContent + " of user " + userIDAuth.getUserID());
        documentSafeService.storeDocument(userIDAuth, modifyNonTxDocument(dsDocument));
    }

    @Override
    public DSDocument nonTxReadDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.debug("read document " + documentFQN + " from folder " + nonTxContent + " of user " + userIDAuth.getUserID());
        return unmodifyNonTxDocument(documentSafeService.readDocument(userIDAuth, modifyNonTxDocumentName(documentFQN)));
    }

    @Override
    public boolean nonTxDocumentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.debug("nonTxDocumentExists " + documentFQN + " from folder " + nonTxContent + " of user " + userIDAuth.getUserID());
        return documentSafeService.documentExists(userIDAuth, modifyNonTxDocumentName(documentFQN));
    }

    @Override
    public void nonTxDeleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.debug("delete document " + documentFQN + " from folder " + nonTxContent + " of user " + userIDAuth.getUserID());
        documentSafeService.deleteDocument(userIDAuth, modifyNonTxDocumentName(documentFQN));
    }

    @Override
    public BucketContentFQN nonTxListDocuments(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        LOGGER.debug("list documents " + documentDirectoryFQN + " from folder " + nonTxContent + " of user " + userIDAuth.getUserID());
        return filterNonTxPrefix(documentSafeService.list(userIDAuth, modifyNonTxDirectoryName(documentDirectoryFQN), recursiveFlag));
    }

    // ============================================================================================
    // NON-TRANSACTIONAL FOR OTHERS
    // ============================================================================================
    @Override
    public void nonTxStoreDocument(UserIDAuth userIDAuth, UserID documentOwner, DSDocument dsDocument) {
        LOGGER.debug("store document " + dsDocument.getDocumentFQN() + " in folder " + nonTxContent + " of user " + documentOwner + " for user " + userIDAuth.getUserID());
        documentSafeService.storeGrantedDocument(userIDAuth, documentOwner, modifyNonTxDocument(dsDocument));
    }

    @Override
    public DSDocument nonTxReadDocument(UserIDAuth userIDAuth, UserID documentOwner, DocumentFQN documentFQN) {
        LOGGER.debug("read document " + documentFQN + " in folder " + nonTxContent + " of user " + documentOwner + " for user " + userIDAuth.getUserID());
        return unmodifyNonTxDocument(documentSafeService.readGrantedDocument(userIDAuth, documentOwner, modifyNonTxDocumentName(documentFQN)));
    }

    @Override
    public boolean nonTxDocumentExists(UserIDAuth userIDAuth, UserID documentOwner, DocumentFQN documentFQN) {
        LOGGER.debug("document exists " + documentFQN + " in folder " + nonTxContent + " of user " + documentOwner + " for user " + userIDAuth.getUserID());
        return documentSafeService.grantedDocumentExists(userIDAuth, documentOwner, modifyNonTxDocumentName(documentFQN));
    }

    private DSDocument modifyNonTxDocument(DSDocument dsDocument) {
        return new DSDocument(
                modifyNonTxDocumentName(dsDocument.getDocumentFQN()),
                dsDocument.getDocumentContent(),
                dsDocument.getDsDocumentMetaInfo());
    }

    private DSDocument unmodifyNonTxDocument(DSDocument dsDocument) {
        return new DSDocument(
                unmodifyNonTxDocumentName(dsDocument.getDocumentFQN()),
                dsDocument.getDocumentContent(),
                dsDocument.getDsDocumentMetaInfo());
    }

    private DocumentFQN modifyNonTxDocumentName(DocumentFQN origName) {
        return nonTxContent.addName(origName);
    }

    private DocumentDirectoryFQN modifyNonTxDirectoryName(DocumentDirectoryFQN origName) {
        return nonTxContent.addDirectory(origName);
    }

    private DocumentDirectoryFQN unmodifyNonTxDocumentDirName(DocumentDirectoryFQN origName) {
        if (origName.getValue().startsWith(nonTxContent.getValue())) {
            return new DocumentDirectoryFQN(origName.getValue().substring(nonTxContent.getValue().length()));
        }
        throw new BaseException("expected " + origName + " to start with " + nonTxContent.getValue());
    }

    private DocumentFQN unmodifyNonTxDocumentName(DocumentFQN origName) {
        if (origName.getValue().startsWith(nonTxContent.getValue())) {
            return new DocumentFQN(origName.getValue().substring(nonTxContent.getValue().length()));
        }
        throw new BaseException("expected " + origName + " to start with " + nonTxContent.getValue());
    }

    // Der echte Pfad soll für den Benutzer transparant sein, daher wird er weggeschnitten
    private BucketContentFQN filterNonTxPrefix(BucketContentFQN list) {
        list.getDirectories().forEach(dir -> LOGGER.debug("before filter:" + dir));
        list.getFiles().forEach(file -> LOGGER.debug("before filter:" + file));
        BucketContentFQN filtered = new BucketContentFQNImpl();
        list.getDirectories().forEach(dir ->
                filtered.getDirectories().add(unmodifyNonTxDocumentDirName(dir)));
        list.getFiles().forEach(file -> filtered.getFiles().add(unmodifyNonTxDocumentName(file)));
        filtered.getDirectories().forEach(dir -> LOGGER.debug("after filter:" + dir));
        filtered.getFiles().forEach(file -> LOGGER.debug("after filter:" + file));
        return filtered;
    }



}
