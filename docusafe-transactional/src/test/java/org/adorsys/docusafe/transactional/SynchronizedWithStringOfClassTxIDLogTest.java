package org.adorsys.docusafe.transactional;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * Created by peter on 03.12.18 14:33.
 * This method proves, that synchronizing the method TxIDLog.saveJustFinishedTx with the userid is ok.
 * The method will become blocked for the same user, but not for different users.
 *
 */
public class SynchronizedWithStringOfClassTxIDLogTest {

    @Test
    public void testMethodsaveJustFinishedTxForSameString() {
        testMethodsaveJustFinishedTx(100, false);
    }

    @Test
    public void testMethodsaveJustFinishedTxForDifferentString() {
        testMethodsaveJustFinishedTx(100, true);
    }

    private void testMethodsaveJustFinishedTx(int WAIT, boolean differ) {
        try {
            String key1 = "affe";
            String key2 = differ ? "nicht affe" : key1;
            Semaphore semaphore = new Semaphore(2);
            CountDownLatch countDownLatch = new CountDownLatch(2);
            semaphore.acquire(2);

            SynchronizedWithStringOfClassTxIDLogTest.ARunnable runnable1 = new SynchronizedWithStringOfClassTxIDLogTest.ARunnable(semaphore, countDownLatch, WAIT, key1);
            SynchronizedWithStringOfClassTxIDLogTest.ARunnable runnable2 = new SynchronizedWithStringOfClassTxIDLogTest.ARunnable(semaphore, countDownLatch, WAIT, key2);
            Thread[] instances = new Thread[2];
            instances[0] = new Thread(runnable1);
            instances[1] = new Thread(runnable2);
            instances[0].start();
            instances[1].start();

            LOGGER.info("prepare for start");
            Thread.currentThread().sleep(2000);
            LOGGER.info("GO");

            semaphore.release(2);
            LOGGER.debug("wait for two instances to finsih");
            countDownLatch.await();

            long fast = Math.min(runnable1.durationInMillis, runnable2.durationInMillis);
            long slow = Math.max(runnable1.durationInMillis, runnable2.durationInMillis);
            LOGGER.info("fast thread took " + fast);
            LOGGER.info("slow thread took " + slow);

            if (differ) {
                Assert.assertTrue(fast < 2 * WAIT);
                Assert.assertTrue(slow < 2 * WAIT);
            } else {
                Assert.assertTrue(fast < 2 * WAIT);
                Assert.assertTrue(slow >= 2 * WAIT);
            }
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }


    }

    public static class ARunnable implements Runnable {
        private int timeToBlock;
        private String key;
        private Semaphore semaphore;
        private CountDownLatch countDownLatch;
        public long durationInMillis = -1;

        public ARunnable(Semaphore sem, CountDownLatch countDownLatch, int timeToBlock, String key) {
            this.timeToBlock = timeToBlock;
            this.key = key;
            this.semaphore = sem;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            try {
                semaphore.acquire();
                LOGGER.info("start for key " + key);
                long start = new Date().getTime();
                blockForTheSameString(key, timeToBlock);
                this.durationInMillis = new Date().getTime() - start;
                LOGGER.info("finsih for key " + key);
                semaphore.release();
            } catch (Exception e) {
                throw BaseExceptionHandler.handle(e);
            } finally {
                countDownLatch.countDown();
            }

        }
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(SynchronizedWithStringOfClassTxIDLogTest.class);

    private static void blockForTheSameString(String s, int timeToBlock) {
        synchronized (s) {
            LOGGER.debug("start method for " + s);
            try {
                Thread.currentThread().sleep(timeToBlock);
            } catch (Exception e) {
                throw BaseExceptionHandler.handle(e);
            }
            LOGGER.debug("finish method for " + s);
        }
    }
}
