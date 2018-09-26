package org.adorsys.docusafe.business;

import com.amazonaws.services.s3.model.Bucket;
import org.adorsys.docusafe.business.exceptions.PathDecryptionException;
import org.adorsys.docusafe.business.impl.caches.BucketPathEncryption;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 25.09.18.
 */
public class BucketPathEncryptionTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(BucketPathEncryption.class);

    @Test
    public void testContainerOnly() {
        BucketPath bucketPath = new BucketPath("peter");
        UserIDAuth userIDAuth = new UserIDAuth(new UserID("aff3"), new ReadKeyPassword("geheim"));
        doTest(bucketPath, userIDAuth);
    }

    @Test
    public void testSimplePath() {
        BucketPath bucketPath = new BucketPath("peter/folder1");
        UserIDAuth userIDAuth = new UserIDAuth(new UserID("aff3"), new ReadKeyPassword("geheim"));
        doTest(bucketPath, userIDAuth);
    }
    @Test
    public void testDeepPathPath() {
        BucketPath bucketPath = new BucketPath("peter/folder1/1/2/3");
        UserIDAuth userIDAuth = new UserIDAuth(new UserID("aff3"), new ReadKeyPassword("geheim"));
        doTest(bucketPath, userIDAuth);
    }

    @Test (expected = PathDecryptionException.class)
    public void testWrongPassword() {
        BucketPath bucketPath = new BucketPath("peter/folder1/1/2/3");
        UserIDAuth userIDAuth1 = new UserIDAuth(new UserID("aff3"), new ReadKeyPassword("geheim1"));
        UserIDAuth userIDAuth2 = new UserIDAuth(new UserID("aff3"), new ReadKeyPassword("geheim2"));
        BucketPath encryptedBucketPath = BucketPathEncryption.encrypt(userIDAuth1, bucketPath);
        BucketPath decryptedBucketPath = BucketPathEncryption.decrypt(userIDAuth2, encryptedBucketPath);
    }

    @Test
    public void lasttest() {
        int NUMBER = 1000;
        BucketPath bucketPath = new BucketPath("peter/folder1/1/2/3");
        UserIDAuth userIDAuth = new UserIDAuth(new UserID("aff3"), new ReadKeyPassword("geheim1"));
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i = 0; i<NUMBER; i++) {
            BucketPath encryptedBucketPath = BucketPathEncryption.encrypt(userIDAuth, bucketPath);
            BucketPath decryptedBucketPath = BucketPathEncryption.decrypt(userIDAuth, encryptedBucketPath);
        }
        stopWatch.stop();
        LOGGER.info("time for " + NUMBER + " en- and decryptions took " + stopWatch.toString());

    }

    private void doTest(BucketPath bucketPath, UserIDAuth userIDAuth) {
        BucketPath encryptedBucketPath = BucketPathEncryption.encrypt(userIDAuth, bucketPath);
        BucketPath decryptedBucketPath = BucketPathEncryption.decrypt(userIDAuth, encryptedBucketPath);
        LOGGER.debug("    plain bucket path:" + bucketPath);
        LOGGER.debug("encrypted bucket path:" + encryptedBucketPath);
        LOGGER.debug("decrypted bucket path:" + decryptedBucketPath);
        Assert.assertEquals(bucketPath, decryptedBucketPath);
        if (BucketPathEncryption.encryptContainer) {
            Assert.assertNotEquals(bucketPath.getObjectHandle().getContainer(), encryptedBucketPath.getObjectHandle().getContainer());
        } else {
            Assert.assertEquals(bucketPath.getObjectHandle().getContainer(), encryptedBucketPath.getObjectHandle().getContainer());
        }
    }

    @Test
    public void dtestContainerOnly() {
        BucketDirectory BucketDirectory = new BucketDirectory("peter");
        UserIDAuth userIDAuth = new UserIDAuth(new UserID("aff3"), new ReadKeyPassword("geheim"));
        doTest(BucketDirectory, userIDAuth);
    }

    @Test
    public void dtestSimplePath() {
        BucketDirectory BucketDirectory = new BucketDirectory("peter/folder1");
        UserIDAuth userIDAuth = new UserIDAuth(new UserID("aff3"), new ReadKeyPassword("geheim"));
        doTest(BucketDirectory, userIDAuth);
    }
    @Test
    public void dtestDeepPathPath() {
        BucketDirectory BucketDirectory = new BucketDirectory("peter/folder1/1/2/3");
        UserIDAuth userIDAuth = new UserIDAuth(new UserID("aff3"), new ReadKeyPassword("geheim"));
        doTest(BucketDirectory, userIDAuth);
    }

    @Test (expected = PathDecryptionException.class)
    public void dtestWrongPassword() {
        BucketDirectory BucketDirectory = new BucketDirectory("peter/folder1/1/2/3");
        UserIDAuth userIDAuth1 = new UserIDAuth(new UserID("aff3"), new ReadKeyPassword("geheim1"));
        UserIDAuth userIDAuth2 = new UserIDAuth(new UserID("aff3"), new ReadKeyPassword("geheim2"));
        BucketDirectory encryptedBucketDirectory = BucketPathEncryption.encrypt(userIDAuth1, BucketDirectory);
        BucketDirectory decryptedBucketDirectory = BucketPathEncryption.decrypt(userIDAuth2, encryptedBucketDirectory);
    }
    private void doTest(BucketDirectory bucketDirectory, UserIDAuth userIDAuth) {
        BucketDirectory encryptedBucketDirectory = BucketPathEncryption.encrypt(userIDAuth, bucketDirectory);
        BucketDirectory decryptedBucketDirectory = BucketPathEncryption.decrypt(userIDAuth, encryptedBucketDirectory);
        LOGGER.debug("    plain bucket path:" + bucketDirectory);
        LOGGER.debug("encrypted bucket path:" + encryptedBucketDirectory);
        LOGGER.debug("decrypted bucket path:" + decryptedBucketDirectory);
        Assert.assertEquals(bucketDirectory, decryptedBucketDirectory);
        if (BucketPathEncryption.encryptContainer) {
            Assert.assertNotEquals(bucketDirectory.getObjectHandle().getContainer(), encryptedBucketDirectory.getObjectHandle().getContainer());
        } else {
            Assert.assertEquals(bucketDirectory.getObjectHandle().getContainer(), encryptedBucketDirectory.getObjectHandle().getContainer());
        }
    }


}
