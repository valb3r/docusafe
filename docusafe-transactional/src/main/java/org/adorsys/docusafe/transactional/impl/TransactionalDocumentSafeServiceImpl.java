package org.adorsys.docusafe.transactional.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.transactional.RequestMemoryContext;
import org.adorsys.docusafe.transactional.TransactionalDocumentSafeService;
import org.adorsys.docusafe.transactional.types.TxBucketContentFQN;
import org.adorsys.docusafe.transactional.exceptions.TxInnerException;
import org.adorsys.docusafe.transactional.exceptions.TxNotActiveException;
import org.adorsys.docusafe.transactional.types.TxDocumentFQNVersion;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.filesystem.exceptions.FileNotFoundException;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.adorsys.encobject.types.PublicKeyJWK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by peter on 11.06.18 at 15:01.
 */
public class TransactionalDocumentSafeServiceImpl extends NonTransactionalDocumentSafeServiceImpl implements TransactionalDocumentSafeService {
    private final static Logger LOGGER = LoggerFactory.getLogger(TransactionalDocumentSafeServiceImpl.class);
    final static DocumentDirectoryFQN txMeta = new DocumentDirectoryFQN("meta.tx");
    final static DocumentDirectoryFQN txContent = new DocumentDirectoryFQN("tx");
    private RequestMemoryContext requestMemoryContext;

    public static final String CURRENT_TRANSACTION_DATA = "CurrentTransactionData";

    public TransactionalDocumentSafeServiceImpl(RequestMemoryContext requestMemoryContext, DocumentSafeService documentSafeService) {
        super(documentSafeService);
        this.requestMemoryContext = requestMemoryContext;
        LOGGER.debug("new Instance of TransactionalDocumentSafeServiceImpl");
    }

    // ============================================================================================
    // TRANSACTIONAL
    // ============================================================================================

    @Override
    public void beginTransaction(UserIDAuth userIDAuth) {
        Date beginTxDate = new Date();
        CurrentTransactionData currentTransactionData = findCurrentTransactionData(userIDAuth.getUserID());
        if (currentTransactionData != null) {
            throw new TxInnerException();
        }

        TxID currentTxID = new TxID();
        LOGGER.debug("beginTransaction " + currentTxID.getValue());
        TxIDHashMap txIDHashMap = TxIDHashMap.fromPreviousFileOrNew(documentSafeService, userIDAuth, currentTxID, beginTxDate);
        currentTransactionData = new CurrentTransactionData(currentTxID, txIDHashMap);
        setCurrentTransactionData(userIDAuth.getUserID(), currentTransactionData);
    }

    @Override
    public void txStoreDocument(UserIDAuth userIDAuth, DSDocument dsDocument) {
        LOGGER.debug("txStoreDocument " + dsDocument.getDocumentFQN().getValue() + " " + getCurrentTxID(userIDAuth.getUserID()));
        documentSafeService.storeDocument(userIDAuth, modifyTxDocument(dsDocument, getCurrentTxID(userIDAuth.getUserID())));
        getCurrentTxIDHashMap(userIDAuth.getUserID()).storeDocument(dsDocument.getDocumentFQN());
    }

    @Override
    public DSDocument txReadDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.debug("txReadDocument " + documentFQN.getValue() + " " + getCurrentTxID(userIDAuth.getUserID()));
        TxID txidOfDocument = getCurrentTxIDHashMap(userIDAuth.getUserID()).readDocument(documentFQN);
        DSDocument dsDocument = documentSafeService.readDocument(userIDAuth, modifyTxDocumentName(documentFQN, txidOfDocument));
        return new DSDocument(documentFQN, dsDocument.getDocumentContent(), dsDocument.getDsDocumentMetaInfo());
    }

    @Override
    public void txDeleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.debug("txDeleteDocument " + documentFQN.getValue() + " " + getCurrentTxID(userIDAuth.getUserID()));
        getCurrentTxIDHashMap(userIDAuth.getUserID()).deleteDocument(documentFQN);
    }

    @Override
    public TxBucketContentFQN txListDocuments(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        LOGGER.debug("txListDocuments " + getCurrentTxID(userIDAuth.getUserID()));
        TxIDHashMap txIDHashMap = getCurrentTxIDHashMap(userIDAuth.getUserID());
        return txIDHashMap.list(documentDirectoryFQN, recursiveFlag);
    }

    @Override
    public TxDocumentFQNVersion getVersion(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        TxBucketContentFQN txBucketContentFQN = txListDocuments(userIDAuth, documentFQN.getDocumentDirectory(), ListRecursiveFlag.FALSE);
        if (txBucketContentFQN.getFilesWithVersion().isEmpty()) {
            throw new FileNotFoundException(documentFQN.getValue(), null);
        }
        return txBucketContentFQN.getFilesWithVersion().stream().findFirst().get().getVersion();
    }

    @Override
    public boolean txDocumentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        LOGGER.debug("txDocumentExists " + documentFQN.getValue() + " " + getCurrentTxID(userIDAuth.getUserID()));
        return getCurrentTxIDHashMap(userIDAuth.getUserID()).documentExists(documentFQN);
    }

    @Override
    public void txDeleteFolder(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN) {
        LOGGER.debug("txDeleteFolder " + documentDirectoryFQN.getValue() + " " + getCurrentTxID(userIDAuth.getUserID()));
        getCurrentTxIDHashMap(userIDAuth.getUserID()).deleteFolder(documentDirectoryFQN);
    }

    @Override
    public void transferFromNonTxToTx(UserIDAuth userIDAuth, DocumentFQN nonTxFQN, DocumentFQN txFQN) {
        DSDocument nonTxDsDocument = nonTxReadDocument(userIDAuth, nonTxFQN);
        DSDocument txDsDocument = new DSDocument(txFQN, nonTxDsDocument.getDocumentContent(), nonTxDsDocument.getDsDocumentMetaInfo());
        txStoreDocument(userIDAuth, txDsDocument);
        getCurrentTransactionData(userIDAuth.getUserID()).addNonTxFileToBeDeletedAfterCommit(nonTxFQN);
    }

    @Override
    public void endTransaction(UserIDAuth userIDAuth) {
        TxID txid = getCurrentTxID(userIDAuth.getUserID());
        LOGGER.debug("endTransaction " + txid.getValue());
        boolean changed = getCurrentTransactionData(userIDAuth.getUserID()).anyDifferenceToInitalState();
        if (changed) {
            LOGGER.info("something has changed, so write down the new state");
            TxIDHashMap txIDHashMap = getCurrentTxIDHashMap(userIDAuth.getUserID());
            txIDHashMap.setEndTransactionDate(new Date());
            txIDHashMap.saveOnce(documentSafeService, userIDAuth);
            txIDHashMap.transactionIsOver(documentSafeService, userIDAuth);
            for (DocumentFQN doc : getCurrentTransactionData(userIDAuth.getUserID()).getNonTxDocumentsToBeDeletedAfterCommit()) {
                try {
                    LOGGER.debug("delete file after commit " + doc);
                    nonTxDeleteDocument(userIDAuth, doc);
                } catch(BaseException e) {
                    LOGGER.warn("Exception is ignored. File deletion after commit does not raise exception");
                } catch (Exception e) {
                    new BaseException(e);
                    LOGGER.warn("Exception is ignored. File deletion after commit does not raise exception");
                }
            }
        } else {
            LOGGER.info("nothing has changed, so nothing has to be written down");
        }
        setCurrentTransactionDataToNull(userIDAuth.getUserID());
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

    private TxID getCurrentTxID(UserID userID) {
        return getCurrentTransactionData(userID).getCurrentTxID();
    }

    private TxIDHashMap getCurrentTxIDHashMap(UserID userID) {
        return getCurrentTransactionData(userID).getCurrentTxHashMap();
    }

    private void setCurrentTransactionDataToNull(UserID userID) {
        setCurrentTransactionData(userID, null);
    }

    @Override
    public PublicKeyJWK findPublicEncryptionKey(UserID userID) {
        return documentSafeService.findPublicEncryptionKey(userID);
    }

    private CurrentTransactionData findCurrentTransactionData(UserID userID) {
        return (CurrentTransactionData) requestMemoryContext.get(CURRENT_TRANSACTION_DATA + "-" + userID.getValue());
    }

    private CurrentTransactionData getCurrentTransactionData(UserID userID) {
        CurrentTransactionData currentTransactionData = findCurrentTransactionData(userID);
        if (currentTransactionData == null) {
            throw new TxNotActiveException(userID);
        }
        return currentTransactionData;
    }

    private void setCurrentTransactionData(UserID userID, CurrentTransactionData currentTransactionData) {
        requestMemoryContext.put(CURRENT_TRANSACTION_DATA  + "-" + userID.getValue(), currentTransactionData);
    }

}
