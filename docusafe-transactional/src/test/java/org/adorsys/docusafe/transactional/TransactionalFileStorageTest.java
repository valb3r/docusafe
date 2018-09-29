package org.adorsys.docusafe.transactional;

import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.docusafe.transactional.exceptions.TxAlreadyClosedException;
import org.adorsys.docusafe.transactional.exceptions.TxNotActiveException;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 12.06.18 at 08:44.
 */
public class TransactionalFileStorageTest extends TransactionFileStorageBaseTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(TransactionalFileStorageTest.class);

    @Test
    public void testOverwrite() {
        transactionalFileStorage.createUser(userIDAuth);
        DocumentFQN documentFQN = new DocumentFQN("testxTFolder/first.txt");
        DocumentContent documentContent = new DocumentContent("very first".getBytes());
        DSDocumentMetaInfo documentMetaInfo = new DSDocumentMetaInfo();
        DSDocument document = new DSDocument(documentFQN, documentContent, documentMetaInfo);

        {
            transactionalFileStorage.beginTransaction(userIDAuth);
            transactionalFileStorage.txStoreDocument(userIDAuth, document);
            transactionalFileStorage.endTransaction(userIDAuth);
        }

        {
            transactionalFileStorage.beginTransaction(userIDAuth);
            BucketContentFQN bucketContentFQN = transactionalFileStorage.txListDocuments(userIDAuth, documentFQN.getDocumentDirectory(), ListRecursiveFlag.TRUE);
            Assert.assertEquals(1, bucketContentFQN.getFiles().size());
            Assert.assertEquals(documentFQN, bucketContentFQN.getFiles().get(0));
            DSDocument dsDocument = transactionalFileStorage.txReadDocument(userIDAuth, documentFQN);
            Assert.assertEquals(documentFQN, dsDocument.getDocumentFQN());
            Assert.assertArrayEquals(document.getDocumentContent().getValue(), dsDocument.getDocumentContent().getValue());
            transactionalFileStorage.endTransaction(userIDAuth);
        }
        DSDocument newDocument = null;
        {
            transactionalFileStorage.beginTransaction(userIDAuth);
            BucketContentFQN bucketContentFQN = transactionalFileStorage.txListDocuments(userIDAuth, documentFQN.getDocumentDirectory(), ListRecursiveFlag.TRUE);
            Assert.assertEquals(1, bucketContentFQN.getFiles().size());
            Assert.assertEquals(documentFQN, bucketContentFQN.getFiles().get(0));
            DSDocument dsDocument = transactionalFileStorage.txReadDocument(userIDAuth, documentFQN);
            newDocument = new DSDocument(documentFQN, new DocumentContent("new content".getBytes()), dsDocument.getDsDocumentMetaInfo());
            transactionalFileStorage.txStoreDocument(userIDAuth, newDocument);
            transactionalFileStorage.endTransaction(userIDAuth);
        }
        {
            transactionalFileStorage.beginTransaction(userIDAuth);
            DSDocument dsDocument = transactionalFileStorage.txReadDocument(userIDAuth, documentFQN);
            Assert.assertArrayEquals(newDocument.getDocumentContent().getValue(), dsDocument.getDocumentContent().getValue());
            transactionalFileStorage.endTransaction(userIDAuth);
        }
    }

    /*
    TODO DOC-48
    @Test
    public void testCreateAndChange() {
        transactionalFileStorage.createUser(userIDAuth);
        DocumentFQN documentFQN = new DocumentFQN("testxTFolder/first.txt");
        DocumentContent documentContent1 = new DocumentContent("very first".getBytes());
        DocumentContent documentContent2 = new DocumentContent("second".getBytes());
        DSDocumentMetaInfo documentMetaInfo = new DSDocumentMetaInfo();
        DSDocument document = new DSDocument(documentFQN, documentContent1, documentMetaInfo);

        // Lege erste Version von first.txt an
        {
            TxID txid = transactionalFileStorage.beginTransaction(userIDAuth);
            LOGGER.debug("FIRST TXID " + txid);
            Assert.assertFalse(transactionalFileStorage.txDocumentExists(txid, userIDAuth, documentFQN));
            transactionalFileStorage.txStoreDocument(txid, userIDAuth, document);
            Assert.assertTrue(transactionalFileStorage.txDocumentExists(txid, userIDAuth, documentFQN));
            transactionalFileStorage.endTransaction(txid, userIDAuth);
        }

        TxID thirdTx = null;
        TxID fourthTx = null;
        // Beginne neue Transaction
        {
            // Überschreibe erste version mit zweiter Version
            TxID txid = transactionalFileStorage.beginTransaction(userIDAuth);
            LOGGER.debug("SECOND TXID " + txid);
            DSDocument dsDocument = transactionalFileStorage.txReadDocument(txid, userIDAuth, documentFQN);
            Assert.assertEquals(new String(documentContent1.getValue()), new String(dsDocument.getDocumentContent().getValue()));
            DSDocument document2 = new DSDocument(documentFQN, documentContent2, documentMetaInfo);
            transactionalFileStorage.txStoreDocument(txid, userIDAuth, document2);
            // Beginne dritte Transaktion VOR Ende der zweiten
            thirdTx = transactionalFileStorage.beginTransaction(userIDAuth);
            LOGGER.debug("THIRD TXID " + thirdTx);
            transactionalFileStorage.endTransaction(txid, userIDAuth);
            // Beginne vierte Transaktion NACH Ende der zweiten
            fourthTx = transactionalFileStorage.beginTransaction(userIDAuth);
            LOGGER.debug("FOURTH TXID " + fourthTx);
        }

        {
            // dritte Tx muss noch ersten Inhalt lesen
            DSDocument dsDocument = transactionalFileStorage.txReadDocument(thirdTx, userIDAuth, documentFQN);
            Assert.assertEquals(new String(documentContent1.getValue()), new String(dsDocument.getDocumentContent().getValue()));
        }

        {
            // vierte Tx muss schon zweiten Inhalt lesen
            DSDocument dsDocument = transactionalFileStorage.txReadDocument(fourthTx, userIDAuth, documentFQN);
            Assert.assertEquals(new String(documentContent2.getValue()), new String(dsDocument.getDocumentContent().getValue()));
        }
        BucketContentFQN list = transactionalFileStorage.txListDocuments(fourthTx, userIDAuth, new DocumentDirectoryFQN("/"), ListRecursiveFlag.TRUE);
        list.getDirectories().forEach(dir -> LOGGER.debug("directory : " + dir));
        list.getFiles().forEach(file -> LOGGER.debug("file:" + file));
        Assert.assertEquals(1, list.getFiles().size());
        Assert.assertEquals(1, list.getDirectories().size());
    }

    @Test
    public void testDelete() {
        transactionalFileStorage.createUser(userIDAuth);
        TxID firstTxID = transactionalFileStorage.beginTransaction(userIDAuth);

        int N = 5;
        {
            // Nun erzeuge N verschiedene Datein in einem Verzeichnis
            for (int i = 0; i < N; i++) {
                DocumentFQN docFQN = new DocumentFQN("folder1/file_" + i + ".txt");
                DocumentContent docContent = new DocumentContent(("Content_" + i).getBytes());
                DSDocumentMetaInfo docMetaInfo = new DSDocumentMetaInfo();
                DSDocument doc = new DSDocument(docFQN, docContent, docMetaInfo);
                transactionalFileStorage.txStoreDocument(firstTxID, userIDAuth, doc);
            }
            transactionalFileStorage.endTransaction(firstTxID, userIDAuth);
        }

        TxID secondTxID;
        TxID thirdTxID;
        TxID fourthTxID;
        {
            // Nun löschen der N verschiedenen Dateien in einer Transaktion
            secondTxID = transactionalFileStorage.beginTransaction(userIDAuth);
            thirdTxID = transactionalFileStorage.beginTransaction(userIDAuth);
            fourthTxID = transactionalFileStorage.beginTransaction(userIDAuth);
            // Nun erzeuge N verschiedene Datein in einem Verzeichnis
            for (int i = 0; i < N; i++) {
                DocumentFQN docFQN = new DocumentFQN("folder1/file_" + i + ".txt");
                Assert.assertTrue(transactionalFileStorage.txDocumentExists(secondTxID, userIDAuth, docFQN));
                transactionalFileStorage.txDeleteDocument(secondTxID, userIDAuth, docFQN);
                Assert.assertFalse(transactionalFileStorage.txDocumentExists(secondTxID, userIDAuth, docFQN));
            }
            transactionalFileStorage.endTransaction(secondTxID, userIDAuth);
        }

        {
            // In der zuvor geöffneten Version müssen diese Dateien noch exisitieren,
            // da sie zum Zeitpunkt des Öffnens der TX noch exisitierten
            for (int i = 0; i < N; i++) {
                DocumentFQN docFQN = new DocumentFQN("folder1/file_" + i + ".txt");
                Assert.assertTrue(transactionalFileStorage.txDocumentExists(thirdTxID, userIDAuth, docFQN));
                Assert.assertTrue(transactionalFileStorage.txDocumentExists(fourthTxID, userIDAuth, docFQN));
            }
            transactionalFileStorage.txDeleteFolder(thirdTxID, userIDAuth, new DocumentDirectoryFQN("folder1"));
            // Nun sind alle Dateien in der thirdTx weg, in der fourthTx aber noch da
            for (int i = 0; i < N; i++) {
                DocumentFQN docFQN = new DocumentFQN("folder1/file_" + i + ".txt");
                Assert.assertFalse(transactionalFileStorage.txDocumentExists(thirdTxID, userIDAuth, docFQN));
                Assert.assertTrue(transactionalFileStorage.txDocumentExists(fourthTxID, userIDAuth, docFQN));
            }
            transactionalFileStorage.endTransaction(thirdTxID, userIDAuth);
            transactionalFileStorage.endTransaction(fourthTxID, userIDAuth);
        }
    }
        */


    @Test(expected = TxNotActiveException.class)
    public void testEndTxTwice() {
        transactionalFileStorage.createUser(userIDAuth);
        transactionalFileStorage.beginTransaction(userIDAuth);
        transactionalFileStorage.endTransaction(userIDAuth);
        transactionalFileStorage.endTransaction(userIDAuth);
    }
}


