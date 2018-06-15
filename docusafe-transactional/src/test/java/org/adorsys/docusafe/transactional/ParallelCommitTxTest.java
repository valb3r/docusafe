package org.adorsys.docusafe.transactional;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.impl.DocumentSafeServiceImpl;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.docusafe.transactional.impl.TransactionalFileStorageImpl;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Security;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * Created by peter on 13.06.18 at 11:38.
 */
// @SuppressWarnings("Duplicates")
public class ParallelCommitTxTest extends TransactionFileStorageBaseTest{
    private final static Logger LOGGER = LoggerFactory.getLogger(ParallelCommitTxTest.class);
    private final static int PARALLEL_INSTANCES = 5;
    private final static String FILENAME = "paralleltest.txt";

    @Test
    public void parallelCommits() {
        try {
            Semaphore semaphore = new Semaphore(PARALLEL_INSTANCES);
            CountDownLatch countDownLatch = new CountDownLatch(PARALLEL_INSTANCES);
            semaphore.acquire(PARALLEL_INSTANCES);
            ARunnable[] runnables = new ARunnable[PARALLEL_INSTANCES];
            Thread[] instances = new Thread[PARALLEL_INSTANCES];
            for (int i = 0; i < PARALLEL_INSTANCES; i++) {
                runnables[i] = new ARunnable(semaphore, countDownLatch, transactionalFileStorage, userIDAuth);
                instances[i] = new Thread(runnables[i]);
                instances[i].start();
            }
            Thread.currentThread().sleep(2000);
            // Lege erste Version von first.txt an
            {
                transactionalFileStorage.createUser(userIDAuth);

                DocumentFQN documentFQN = new DocumentFQN(FILENAME);
                DocumentContent documentContent = new DocumentContent("very first".getBytes());
                DSDocumentMetaInfo documentMetaInfo = new DSDocumentMetaInfo();
                DSDocument document = new DSDocument(documentFQN, documentContent, documentMetaInfo);

                TxID txid = transactionalFileStorage.beginTransaction(userIDAuth);
                LOGGER.debug("FIRST TXID " + txid);
                Assert.assertFalse(transactionalFileStorage.documentExists(txid, userIDAuth, documentFQN));
                transactionalFileStorage.storeDocument(txid, userIDAuth, document);
                Assert.assertTrue(transactionalFileStorage.documentExists(txid, userIDAuth, documentFQN));
                transactionalFileStorage.endTransaction(txid, userIDAuth);
            }

            LOGGER.debug("start " + PARALLEL_INSTANCES + " threads concurrently now");
            semaphore.release(PARALLEL_INSTANCES);
            LOGGER.debug("wait for " + PARALLEL_INSTANCES + " to finsih");
            countDownLatch.await();
            LOGGER.debug(PARALLEL_INSTANCES + " threadas have finished");

            int errorCounter = 0;
            for (int i = 0; i < PARALLEL_INSTANCES; i++) {
                if (! runnables[i].ok) {
                    errorCounter ++;
                    LOGGER.error("error " + errorCounter + " " + runnables[i].exception.getMessage());
                }
            }
            // Assert.assertEquals(0, errorCounter);

        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public static class ARunnable implements Runnable {
        private final static Logger LOGGER = LoggerFactory.getLogger(ARunnable.class);
        private static int instanceCounter = 0;

        private int instanceID;
        private Semaphore sem;
        private TransactionalFileStorage transactionalFileStorage;
        private UserIDAuth userIDAuth;
        private CountDownLatch countDownLatch;
        public boolean ok = false;
        public Exception exception;

        public ARunnable(Semaphore sem, CountDownLatch countDownLatch, TransactionalFileStorage transactionalFileStorage, UserIDAuth userIDAuth) {
            this.instanceID = instanceCounter++;
            this.sem = sem;
            this.transactionalFileStorage = transactionalFileStorage;
            this.userIDAuth = userIDAuth;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            try {
                DocumentFQN documentFQN = new DocumentFQN(FILENAME);
                DocumentContent documentContent = new DocumentContent(("Thread Number " + instanceID).getBytes());
                DSDocumentMetaInfo documentMetaInfo = new DSDocumentMetaInfo();
                DSDocument document = new DSDocument(documentFQN, documentContent, documentMetaInfo);

                sem.acquire();

                TxID txid = transactionalFileStorage.beginTransaction(userIDAuth);
                transactionalFileStorage.storeDocument(txid, userIDAuth, document);
                transactionalFileStorage.endTransaction(txid, userIDAuth);

                sem.release();
                ok = true;
            } catch (Exception e) {
                exception = e;
            } finally {
                countDownLatch.countDown();
            }
        }
    }

}
