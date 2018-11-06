package org.adorsys.docusafe.transactional;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.docusafe.transactional.exceptions.TxAlreadyClosedException;
import org.adorsys.docusafe.transactional.exceptions.TxInnerException;
import org.adorsys.docusafe.transactional.exceptions.TxNotActiveException;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.bouncycastle.jcajce.provider.symmetric.ARC4;
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

    @Test (expected = TxInnerException.class)
    public void innerTxNotImplementedYet() {
        

        transactionalFileStorage.createUser(userIDAuth);
        transactionalFileStorage.beginTransaction(userIDAuth);
        transactionalFileStorage.beginTransaction(userIDAuth);
    }

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
            transactionalFileStorage.beginTransaction(userIDAuth);
            LOGGER.debug("FIRST TXID ");
            Assert.assertFalse(transactionalFileStorage.txDocumentExists(userIDAuth, documentFQN));
            transactionalFileStorage.txStoreDocument(userIDAuth, document);
            Assert.assertTrue(transactionalFileStorage.txDocumentExists(userIDAuth, documentFQN));
            transactionalFileStorage.endTransaction(userIDAuth);
        }

        // Beginne neue Transaction
        {
            // Überschreibe erste version mit zweiter Version
            requestMemoryContext.switchToUser(2);
            transactionalFileStorage.beginTransaction(userIDAuth);
            LOGGER.debug("SECOND TXID ");
            DSDocument dsDocument = transactionalFileStorage.txReadDocument(userIDAuth, documentFQN);
            Assert.assertEquals(new String(documentContent1.getValue()), new String(dsDocument.getDocumentContent().getValue()));
            DSDocument document2 = new DSDocument(documentFQN, documentContent2, documentMetaInfo);
            transactionalFileStorage.txStoreDocument(userIDAuth, document2);

            // Beginne dritte Transaktion VOR Ende der zweiten
            requestMemoryContext.switchToUser(3);
            transactionalFileStorage.beginTransaction(userIDAuth);
            LOGGER.debug("THIRD TXID ");
            requestMemoryContext.switchToUser(2);
            transactionalFileStorage.endTransaction(userIDAuth);

            // Beginne vierte Transaktion NACH Ende der zweiten
            requestMemoryContext.switchToUser(4);
            transactionalFileStorage.beginTransaction(userIDAuth);
            LOGGER.debug("FOURTH TXID ");
        }

        {
            // dritte Tx muss noch ersten Inhalt lesen
            requestMemoryContext.switchToUser(3);
            DSDocument dsDocument = transactionalFileStorage.txReadDocument(userIDAuth, documentFQN);
            Assert.assertEquals(new String(documentContent1.getValue()), new String(dsDocument.getDocumentContent().getValue()));
        }

        {
            // vierte Tx muss schon zweiten Inhalt lesen
            requestMemoryContext.switchToUser(4);
            DSDocument dsDocument = transactionalFileStorage.txReadDocument(userIDAuth, documentFQN);
            Assert.assertEquals(new String(documentContent2.getValue()), new String(dsDocument.getDocumentContent().getValue()));
        }
        requestMemoryContext.switchToUser(4);
        BucketContentFQN list = transactionalFileStorage.txListDocuments(userIDAuth, new DocumentDirectoryFQN("/"), ListRecursiveFlag.TRUE);
        list.getDirectories().forEach(dir -> LOGGER.debug("directory : " + dir));
        list.getFiles().forEach(file -> LOGGER.debug("file:" + file));
        Assert.assertEquals(1, list.getFiles().size());
        Assert.assertEquals(1, list.getDirectories().size());
    }

    @Test
    public void testDelete() {
        

        transactionalFileStorage.createUser(userIDAuth);
        transactionalFileStorage.beginTransaction(userIDAuth);

        int N = 5;
        // TODO actually a performance test
        N=2;
        {
            // Nun erzeuge N verschiedene Datein in einem Verzeichnis
            for (int i = 0; i < N; i++) {
                DocumentFQN docFQN = new DocumentFQN("folder1/file_" + i + ".txt");
                DocumentContent docContent = new DocumentContent(("Content_" + i).getBytes());
                DSDocumentMetaInfo docMetaInfo = new DSDocumentMetaInfo();
                DSDocument doc = new DSDocument(docFQN, docContent, docMetaInfo);
                transactionalFileStorage.txStoreDocument(userIDAuth, doc);
            }
            transactionalFileStorage.endTransaction(userIDAuth);
        }

        {
            // Nun löschen der N verschiedenen Dateien in einer Transaktion
            requestMemoryContext.switchToUser(2);
            transactionalFileStorage.beginTransaction(userIDAuth);
            requestMemoryContext.switchToUser(3);
            transactionalFileStorage.beginTransaction(userIDAuth);
            requestMemoryContext.switchToUser(4);
            transactionalFileStorage.beginTransaction(userIDAuth);
            // Nun erzeuge N verschiedene Datein in einem Verzeichnis
            for (int i = 0; i < N; i++) {
                DocumentFQN docFQN = new DocumentFQN("folder1/file_" + i + ".txt");
                requestMemoryContext.switchToUser(2);
                Assert.assertTrue(transactionalFileStorage.txDocumentExists(userIDAuth, docFQN));
                transactionalFileStorage.txDeleteDocument(userIDAuth, docFQN);
                Assert.assertFalse(transactionalFileStorage.txDocumentExists(userIDAuth, docFQN));
            }
            transactionalFileStorage.endTransaction(userIDAuth);
        }

        {
            // In der zuvor geöffneten Version müssen diese Dateien noch exisitieren,
            // da sie zum Zeitpunkt des Öffnens der TX noch exisitierten
            for (int i = 0; i < N; i++) {
                DocumentFQN docFQN = new DocumentFQN("folder1/file_" + i + ".txt");
                requestMemoryContext.switchToUser(3);
                Assert.assertTrue(transactionalFileStorage.txDocumentExists(userIDAuth, docFQN));
                requestMemoryContext.switchToUser(4);
                Assert.assertTrue(transactionalFileStorage.txDocumentExists(userIDAuth, docFQN));
            }
            requestMemoryContext.switchToUser(3);
            transactionalFileStorage.txDeleteFolder(userIDAuth, new DocumentDirectoryFQN("folder1"));
            // Nun sind alle Dateien in der thirdTx weg, in der fourthTx aber noch da
            for (int i = 0; i < N; i++) {
                DocumentFQN docFQN = new DocumentFQN("folder1/file_" + i + ".txt");
                requestMemoryContext.switchToUser(3);
                Assert.assertFalse(transactionalFileStorage.txDocumentExists(userIDAuth, docFQN));
                requestMemoryContext.switchToUser(4);
                Assert.assertTrue(transactionalFileStorage.txDocumentExists(userIDAuth, docFQN));
            }
            requestMemoryContext.switchToUser(3);
            transactionalFileStorage.endTransaction(userIDAuth);
            requestMemoryContext.switchToUser(4);
            transactionalFileStorage.endTransaction(userIDAuth);
        }
    }


    @Test(expected = TxNotActiveException.class)
    public void testEndTxTwice() {
        

        transactionalFileStorage.createUser(userIDAuth);
        transactionalFileStorage.beginTransaction(userIDAuth);
        transactionalFileStorage.endTransaction(userIDAuth);
        transactionalFileStorage.endTransaction(userIDAuth);
    }
}


