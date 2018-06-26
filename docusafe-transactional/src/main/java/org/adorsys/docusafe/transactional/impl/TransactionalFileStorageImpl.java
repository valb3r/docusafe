package org.adorsys.docusafe.transactional.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.impl.BucketContentFQNImpl;
import org.adorsys.docusafe.business.impl.DocumentSafeServiceImpl;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.service.types.AccessType;
import org.adorsys.docusafe.transactional.TransactionalFileStorage;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by peter on 11.06.18 at 15:01.
 */
public class TransactionalFileStorageImpl implements TransactionalFileStorage {
    private final static Logger LOGGER = LoggerFactory.getLogger(TransactionalFileStorageImpl.class);
    final static DocumentDirectoryFQN txMeta = new DocumentDirectoryFQN("meta.tx");
    final static DocumentDirectoryFQN txContent = new DocumentDirectoryFQN("tx");
    final static DocumentDirectoryFQN nonTxContent = new DocumentDirectoryFQN("nonttx");
    private DocumentSafeService documentSafeService;

    public TransactionalFileStorageImpl(ExtendedStoreConnection extendedStoreConnection) {
        LOGGER.debug("new Instance of TransactionalFileStorageImpl");
        documentSafeService = new DocumentSafeServiceImpl(extendedStoreConnection);
    }

    // ============================================================================================
    // NON-TRANSACTIONAL
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
    public void grantAccess(UserIDAuth userIDAuth, UserID receiverUserID) {
        LOGGER.debug("grant write access from " + userIDAuth.getUserID() + " to " + receiverUserID + " for " + nonTxContent);
        documentSafeService.grantAccessToUserForFolder(userIDAuth, receiverUserID, nonTxContent, AccessType.WRITE);
    }

    @Override
    public void storeDocument(UserIDAuth userIDAuth, UserID documentOwner, DSDocument dsDocument) {
        LOGGER.debug("store document " + dsDocument.getDocumentFQN() + " in folder " + nonTxContent + " of user " + documentOwner);
        documentSafeService.storeGrantedDocument(userIDAuth, documentOwner, modifyNonTxDocument(dsDocument));
    }


    @Override
    public DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.debug("read document " + documentFQN + " from folder " + nonTxContent + " of user " + userIDAuth.getUserID());
        return unmodifyNonTxDocument(documentSafeService.readDocument(userIDAuth, modifyNonTxDocumentName(documentFQN)));
    }

    @Override
    public void deleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.debug("delete document " + documentFQN + " from folder " + nonTxContent + " of user " + userIDAuth.getUserID());
        documentSafeService.deleteDocument(userIDAuth, modifyNonTxDocumentName(documentFQN));
    }

    @Override
    public BucketContentFQN listDocuments(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        LOGGER.debug("list documents " + documentDirectoryFQN + " from folder " + nonTxContent + " of user " + userIDAuth.getUserID());
        return filterNonTxPrefix(documentSafeService.list(userIDAuth, modifyNonTxDirectoryName(documentDirectoryFQN), recursiveFlag));
    }

    // ============================================================================================
    // TRANSACTIONAL
    // ============================================================================================

    @Override
    public TxID beginTransaction(UserIDAuth userIDAuth) {
        Date beginTxDate = new Date();
        TxID currentTxID = new TxID();
        LOGGER.debug("beginTransaction " + currentTxID.getValue());
        TxIDHashMap txIDHashMap = TxIDHashMap.fromPreviousFileOrNew(documentSafeService, userIDAuth, currentTxID, beginTxDate);
        txIDHashMap.save(documentSafeService, userIDAuth);
        return currentTxID;
    }

    @Override
    public void storeDocument(TxID txid, UserIDAuth userIDAuth, DSDocument dsDocument) {
        LOGGER.debug("storeDocument " + dsDocument.getDocumentFQN().getValue() + " " + txid.getValue());
        TxIDHashMap txIDHashMap = TxIDHashMap.getCurrentFile(documentSafeService, userIDAuth, txid);
        documentSafeService.storeDocument(userIDAuth, modifyTxDocument(dsDocument, txid));
        txIDHashMap.storeDocument(dsDocument.getDocumentFQN());
        txIDHashMap.save(documentSafeService, userIDAuth);
    }


    @Override
    public DSDocument readDocument(TxID txid, UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.debug("readDocument " + documentFQN.getValue() + " " + txid.getValue());
        TxIDHashMap txIDHashMap = TxIDHashMap.getCurrentFile(documentSafeService, userIDAuth, txid);
        TxID txidOfDocument = txIDHashMap.readDocument(documentFQN);
        return documentSafeService.readDocument(userIDAuth, modifyTxDocumentName(documentFQN, txidOfDocument));
    }

    @Override
    public void deleteDocument(TxID txid, UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.debug("deleteDocument " + documentFQN.getValue() + " " + txid.getValue());
        TxIDHashMap txIDHashMap = TxIDHashMap.getCurrentFile(documentSafeService, userIDAuth, txid);
        txIDHashMap.save(documentSafeService, userIDAuth);
        txIDHashMap.deleteDocument(documentFQN);
        txIDHashMap.save(documentSafeService, userIDAuth);
    }

    @Override
    public BucketContentFQN listDocuments(TxID txid, UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        TxIDHashMap txIDHashMap = TxIDHashMap.getCurrentFile(documentSafeService, userIDAuth, txid);
        return txIDHashMap.list(documentDirectoryFQN, recursiveFlag);
    }

    @Override
    public boolean documentExists(TxID txid, UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.debug("documentExists " + documentFQN.getValue() + " " + txid.getValue());
        TxIDHashMap txIDHashMap = TxIDHashMap.getCurrentFile(documentSafeService, userIDAuth, txid);
        return txIDHashMap.documentExists(documentFQN);
    }

    @Override
    public void deleteFolder(TxID txid, UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN) {
        LOGGER.debug("deleteFolder " + documentDirectoryFQN.getValue() + " " + txid.getValue());
        TxIDHashMap txIDHashMap = TxIDHashMap.getCurrentFile(documentSafeService, userIDAuth, txid);
        txIDHashMap.deleteFolder(documentDirectoryFQN);
        txIDHashMap.save(documentSafeService, userIDAuth);
    }

    @Override
    public void endTransaction(TxID txid, UserIDAuth userIDAuth) {
        LOGGER.debug("transactionIsOver " + txid.getValue());
        TxIDHashMap txIDHashMap = TxIDHashMap.getCurrentFile(documentSafeService, userIDAuth, txid);
        txIDHashMap.setEndTransactionDate(new Date());
        txIDHashMap.save(documentSafeService, userIDAuth);
        txIDHashMap.transactionIsOver(documentSafeService, userIDAuth);
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

    public static DSDocument modifyTxDocument(DSDocument dsDocument, TxID txid) {
        return new DSDocument(
                modifyTxDocumentName(dsDocument.getDocumentFQN(), txid),
                dsDocument.getDocumentContent(),
                dsDocument.getDsDocumentMetaInfo());
    }

    public static DocumentFQN modifyTxDocumentName(DocumentFQN origName, TxID txid) {
        return txContent.addName(origName.getValue() + "." + txid.getValue());
    }

    public static DocumentFQN modifyTxMetaDocumentName(DocumentFQN origName, TxID txid) {
        return txMeta.addName(origName.getValue() + "." + txid.getValue());
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

    private DocumentFQN unmodifyNonTxDocumentName(DocumentFQN origName) {
        if (origName.getValue().startsWith(nonTxContent.getValue())) {
            return new DocumentFQN(origName.getValue().substring(nonTxContent.getValue().length()));
        }
        throw new BaseException("expected " + origName + " to start with " + nonTxContent.getValue());
    }

    private DocumentDirectoryFQN unmodifyNonTxDocumentDirName(DocumentDirectoryFQN origName) {
        if (origName.getValue().startsWith(nonTxContent.getValue())) {
            return new DocumentDirectoryFQN(origName.getValue().substring(nonTxContent.getValue().length()));
        }
        throw new BaseException("expected " + origName + " to start with " + nonTxContent.getValue());
    }

    private DocumentFQN modifyNonTxDocumentName(DocumentFQN origName) {
        return nonTxContent.addName(origName);
    }

    private DocumentDirectoryFQN modifyNonTxDirectoryName(DocumentDirectoryFQN origName) {
        return nonTxContent.addDirectory(origName);
    }
}
