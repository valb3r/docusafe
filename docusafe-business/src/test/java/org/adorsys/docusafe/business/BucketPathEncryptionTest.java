package org.adorsys.docusafe.business;

import org.adorsys.docusafe.business.exceptions.PathDecryptionException;
import org.adorsys.docusafe.business.impl.caches.BucketPathEncryption;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.ReadKeyPassword;
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
        ReadKeyPassword readKeyPassword = new ReadKeyPassword("geheim");
        doTest(bucketPath, readKeyPassword);
    }

    @Test
    public void testSimplePath() {
        BucketPath bucketPath = new BucketPath("peter/folder1");
        ReadKeyPassword readKeyPassword = new ReadKeyPassword("geheim");
        doTest(bucketPath, readKeyPassword);
    }
    @Test
    public void testDeepPathPath() {
        BucketPath bucketPath = new BucketPath("peter/folder1/1/2/3");
        ReadKeyPassword readKeyPassword = new ReadKeyPassword("geheim");
        doTest(bucketPath, readKeyPassword);
    }

    @Test (expected = PathDecryptionException.class)
    public void testWrongPassword() {
        BucketPath bucketPath = new BucketPath("peter/folder1/1/2/3");
        ReadKeyPassword readKeyPassword1 = new ReadKeyPassword("geheim1");
        ReadKeyPassword readKeyPassword2 = new ReadKeyPassword("geheim2");
        BucketPath encryptedBucketPath = BucketPathEncryption.encrypt(readKeyPassword1, bucketPath);
        BucketPath decryptedBucketPath = BucketPathEncryption.decrypt(readKeyPassword2, encryptedBucketPath);
    }

    private void doTest(BucketPath bucketPath, ReadKeyPassword readKeyPassword) {
        BucketPath encryptedBucketPath = BucketPathEncryption.encrypt(readKeyPassword, bucketPath);
        BucketPath decryptedBucketPath = BucketPathEncryption.decrypt(readKeyPassword, encryptedBucketPath);
        LOGGER.debug("    plain bucket path:" + bucketPath);
        LOGGER.debug("encrypted bucket path:" + encryptedBucketPath);
        LOGGER.debug("decrypted bucket path:" + decryptedBucketPath);
        Assert.assertEquals(bucketPath, decryptedBucketPath);
    }

    @Test
    public void dtestContainerOnly() {
        BucketDirectory BucketDirectory = new BucketDirectory("peter");
        ReadKeyPassword readKeyPassword = new ReadKeyPassword("geheim");
        doTest(BucketDirectory, readKeyPassword);
    }

    @Test
    public void dtestSimplePath() {
        BucketDirectory BucketDirectory = new BucketDirectory("peter/folder1");
        ReadKeyPassword readKeyPassword = new ReadKeyPassword("geheim");
        doTest(BucketDirectory, readKeyPassword);
    }
    @Test
    public void dtestDeepPathPath() {
        BucketDirectory BucketDirectory = new BucketDirectory("peter/folder1/1/2/3");
        ReadKeyPassword readKeyPassword = new ReadKeyPassword("geheim");
        doTest(BucketDirectory, readKeyPassword);
    }

    @Test (expected = PathDecryptionException.class)
    public void dtestWrongPassword() {
        BucketDirectory BucketDirectory = new BucketDirectory("peter/folder1/1/2/3");
        ReadKeyPassword readKeyPassword1 = new ReadKeyPassword("geheim1");
        ReadKeyPassword readKeyPassword2 = new ReadKeyPassword("geheim2");
        BucketDirectory encryptedBucketDirectory = BucketPathEncryption.encrypt(readKeyPassword1, BucketDirectory);
        BucketDirectory decryptedBucketDirectory = BucketPathEncryption.decrypt(readKeyPassword2, encryptedBucketDirectory);
    }

    private void doTest(BucketDirectory BucketDirectory, ReadKeyPassword readKeyPassword) {
        BucketDirectory encryptedBucketDirectory = BucketPathEncryption.encrypt(readKeyPassword, BucketDirectory);
        BucketDirectory decryptedBucketDirectory = BucketPathEncryption.decrypt(readKeyPassword, encryptedBucketDirectory);
        LOGGER.debug("    plain bucket path:" + BucketDirectory);
        LOGGER.debug("encrypted bucket path:" + encryptedBucketDirectory);
        LOGGER.debug("decrypted bucket path:" + decryptedBucketDirectory);
        Assert.assertEquals(BucketDirectory, decryptedBucketDirectory);
    }
}
