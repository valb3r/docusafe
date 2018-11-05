package org.adorsys.docusafe.transactional;

import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by peter on 11.07.18 at 11:20.
 */
public class TxHistoryCleanupTest extends TransactionFileStorageBaseTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(TxHistoryCleanupTest.class);

    @Test
    public void createFilesAndDeleteSomeRandomFilesInServeralTransactions() {
        

        StopWatch st = new StopWatch();
        st.start();

        Map<DocumentFQN, DocumentContent> memoryMap = new HashMap<>();

        int numberOfTransactinos = 9;
        int numberOfFilesToDeletePerTx = 1;
        int numberOfFilesToCreatePerTx = 4;
        int numberOfFilesToOverwritePerTx = 4;
        int expectedNumberOfFilesAfterIteration = (numberOfFilesToCreatePerTx * numberOfTransactinos) - (numberOfTransactinos * numberOfFilesToDeletePerTx);

        transactionalFileStorage.createUser(userIDAuth);
        DocumentDirectoryFQN documentDirectoryFQN = new DocumentDirectoryFQN("folder");

        int staticCounter = 0;
        {
            // create documents
            for (int i = 0; i < numberOfTransactinos; i++) {
                transactionalFileStorage.beginTransaction(userIDAuth);
                for (int j = 0; j < numberOfFilesToCreatePerTx; j++) {
                    DSDocument document = new DSDocument(documentDirectoryFQN.addName("file_" + staticCounter++ + ".TXT"),
                            new DocumentContent(("Content of File " + i).getBytes()),
                            new DSDocumentMetaInfo());
                    transactionalFileStorage.txStoreDocument(userIDAuth, document);
                    memoryMap.put(document.getDocumentFQN(), document.getDocumentContent());
                }
                transactionalFileStorage.endTransaction(userIDAuth);
            }
        }
        {
            // delete documentes
            for (int i = 0; i < numberOfTransactinos; i++) {
                transactionalFileStorage.beginTransaction(userIDAuth);
                for (int j = 0; j < numberOfFilesToDeletePerTx; j++) {
                    BucketContentFQN bucketContentFQN = transactionalFileStorage.txListDocuments(userIDAuth, documentDirectoryFQN, ListRecursiveFlag.TRUE);
                    int currentNumberOfFiles = bucketContentFQN.getFiles().size();
                    int indexToDelete = getRandomInRange(currentNumberOfFiles);
                    LOGGER.debug("Transaction number " + i + " has " + currentNumberOfFiles + " files");
                    LOGGER.debug("Index to delete is " + indexToDelete);
                    transactionalFileStorage.txDeleteDocument(userIDAuth, bucketContentFQN.getFiles().get(indexToDelete));
                    memoryMap.remove(bucketContentFQN.getFiles().get(indexToDelete));

                }
                transactionalFileStorage.endTransaction(userIDAuth);
            }
        }
        {
            // overwrite documents
            for (int i = 0; i < numberOfTransactinos; i++) {
                transactionalFileStorage.beginTransaction(userIDAuth);
                for (int j = 0; j < numberOfFilesToOverwritePerTx; j++) {
                    BucketContentFQN bucketContentFQN = transactionalFileStorage.txListDocuments(userIDAuth, documentDirectoryFQN, ListRecursiveFlag.TRUE);
                    int currentNumberOfFiles = bucketContentFQN.getFiles().size();
                    int indexToOverwrite = getRandomInRange(currentNumberOfFiles);
                    DSDocument dsDocument = transactionalFileStorage.txReadDocument(userIDAuth, bucketContentFQN.getFiles().get(indexToOverwrite));
                    DSDocument newDsDocument = new DSDocument(dsDocument.getDocumentFQN(),
                            new DocumentContent((new String(dsDocument.getDocumentContent().getValue()) + " overwritten in tx ").getBytes()),
                            new DSDocumentMetaInfo());
                    transactionalFileStorage.txStoreDocument(userIDAuth, newDsDocument);
                    memoryMap.put(newDsDocument.getDocumentFQN(), newDsDocument.getDocumentContent());
                }
                transactionalFileStorage.endTransaction(userIDAuth);
            }
        }
        {
            transactionalFileStorage.beginTransaction(userIDAuth);
            BucketContentFQN bucketContentFQN = transactionalFileStorage.txListDocuments(userIDAuth, documentDirectoryFQN, ListRecursiveFlag.TRUE);
            LOGGER.debug("LIST OF FILES IN TRANSACTIONAL LAYER: " + bucketContentFQN.toString());
            Assert.assertEquals(memoryMap.keySet().size(), bucketContentFQN.getFiles().size());
            bucketContentFQN.getFiles().forEach(documentFQN -> {
                DSDocument dsDocument = transactionalFileStorage.txReadDocument(userIDAuth, documentFQN);
                Assert.assertArrayEquals(memoryMap.get(documentFQN).getValue(), dsDocument.getDocumentContent().getValue());
                LOGGER.debug(documentFQN + " checked!");
            });
            transactionalFileStorage.endTransaction(userIDAuth);
            Assert.assertEquals(expectedNumberOfFilesAfterIteration, bucketContentFQN.getFiles().size());
        }

        // Nun gehen wir direkt auf das Filesystem. Hier gibt es nun alle Dateien zu sehen
        BucketContentFQN list = dssi.list(userIDAuth, new DocumentDirectoryFQN("/"), ListRecursiveFlag.TRUE);
        LOGGER.debug("LIST OF FILES IN DOCUMENTSAFE: " + list.toString());
//        Assert.assertEquals(numberOfFiles, list.getFiles().size());
        st.stop();
        LOGGER.debug("time for test " + st.toString());
    }


    private int getRandomInRange(int max) {
        // nextInt is normally exclusive of the top value,
        int randomNum = ThreadLocalRandom.current().nextInt(0, max);
        return randomNum;
    }
}
