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
import org.adorsys.docusafe.transactional.TransactionalFileStorage;
import org.adorsys.docusafe.transactional.exceptions.TxGrantedDocumentMustNotContainFolderException;
import org.adorsys.docusafe.transactional.impl.helper.TxIDVersionHelper;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.stream.Collectors;

/**
 * Created by peter on 11.06.18 at 15:01.
 */
public class TransactionalFileStorageImpl implements TransactionalFileStorage {
    private final static Logger LOGGER = LoggerFactory.getLogger(TransactionalFileStorageImpl.class);
    final static DocumentDirectoryFQN txdir = new DocumentDirectoryFQN(TransactionalFileStorage.class.getPackage().getName().toString());


    private DocumentSafeService documentSafeService;
    private DocumentDirectoryFQN inboxFolder;

    public TransactionalFileStorageImpl(DocumentSafeService documentSafeService) {
        this(documentSafeService, new DocumentDirectoryFQN("INBOX"));
    }

    public TransactionalFileStorageImpl(DocumentSafeService documentSafeService, DocumentDirectoryFQN inboxFolder) {
        LOGGER.debug("new Instance of TransactionalFileStorageImpl");
        this.documentSafeService = documentSafeService;
        this.inboxFolder = inboxFolder;
    }

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
    public void grantAccessToUserForInboxFolder(UserIDAuth userIDAuth, UserID receiverUserID) {
        LOGGER.debug("grant write access from " + userIDAuth.getUserID() + " to " + receiverUserID + " for " + inboxFolder);
        documentSafeService.grantAccessToUserForFolder(userIDAuth, receiverUserID, inboxFolder, AccessType.WRITE);
    }

    @Override
    public void storeDocumentInInputFolder(UserIDAuth userIDAuth, UserID documentOwner, DSDocument dsDocument) {
        String file = dsDocument.getDocumentFQN().getValue();
        DocumentFQN userSpecificFQN = inboxFolder.addName(file);
        LOGGER.debug("store document " + file + " in folder " + inboxFolder + " of user " + documentOwner);
        documentSafeService.storeGrantedDocument(userIDAuth, documentOwner, new DSDocument(userSpecificFQN, dsDocument.getDocumentContent(), dsDocument.getDsDocumentMetaInfo()));
    }

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
        documentSafeService.storeDocument(userIDAuth, modify(dsDocument, txid));
        txIDHashMap.storeDocument(dsDocument.getDocumentFQN());
        txIDHashMap.save(documentSafeService, userIDAuth);
    }


    @Override
    public DSDocument readDocument(TxID txid, UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.debug("readDocument " + documentFQN.getValue() + " " + txid.getValue());
        TxIDHashMap txIDHashMap = TxIDHashMap.getCurrentFile(documentSafeService, userIDAuth, txid);
        TxID txidOfDocument = txIDHashMap.readDocument(documentFQN);
        return documentSafeService.readDocument(userIDAuth, TxIDVersionHelper.get(documentFQN, txidOfDocument));
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

    @Override
    public BucketContentFQN list(TxID txid, UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        if (documentDirectoryFQN.getValue().startsWith(inboxFolder.getValue())) {
            // list physical files
            return filterTxDir(documentSafeService.list(userIDAuth, documentDirectoryFQN, recursiveFlag));
        }
        if (documentDirectoryFQN.getValue().length() > 1) {
            // return HashMap Only
            TxIDHashMap txIDHashMap = TxIDHashMap.getCurrentFile(documentSafeService, userIDAuth, txid);
            return txIDHashMap.list(documentDirectoryFQN, recursiveFlag);
        }

        if (documentDirectoryFQN.getValue().length() == 1) {
            // nun also die Summe aus inbox und hashMap
            BucketContentFQN bucketContentFQNInbox = filterTxDir(documentSafeService.list(userIDAuth, inboxFolder, recursiveFlag));

            TxIDHashMap txIDHashMap = TxIDHashMap.getCurrentFile(documentSafeService, userIDAuth, txid);
            BucketContentFQN bucketContentFQNHashMap = txIDHashMap.list(documentDirectoryFQN, recursiveFlag);

            BucketContentFQN sum = new BucketContentFQNImpl();
            sum.getFiles().addAll(bucketContentFQNInbox.getFiles());
            sum.getFiles().addAll(bucketContentFQNHashMap.getFiles());
            sum.getDirectories().addAll(bucketContentFQNInbox.getDirectories());
            sum.getDirectories().addAll(bucketContentFQNHashMap.getDirectories());
            return sum;
        }

        throw new BaseException("Programming Error. Did not expect this " + documentDirectoryFQN);
    }

    private BucketContentFQN filterTxDir(BucketContentFQN list) {
        BucketContentFQN filtered = new BucketContentFQNImpl();
        filtered.getDirectories().addAll(
                list.getDirectories().stream().filter(
                        dir -> ! dir.getValue().startsWith(txdir.getValue())).collect(Collectors.toList()));
        filtered.getFiles().addAll(
                list.getFiles().stream().filter(
                        file -> ! file.getValue().startsWith(txdir.getValue())).collect(Collectors.toList()));
        return filtered;
    }


    private DSDocument modify(DSDocument dsDocument, TxID txid) {
        return new DSDocument(
                modify(dsDocument.getDocumentFQN(), txid),
                dsDocument.getDocumentContent(),
                dsDocument.getDsDocumentMetaInfo());
    }

    private DocumentFQN modify(DocumentFQN documentFQN, TxID txid) {
        return TxIDVersionHelper.get(documentFQN, txid);
    }
}
