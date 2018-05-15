package org.adorsys.docusafe.business;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.utils.GrantUtil;
import org.adorsys.docusafe.business.utils.UserIDUtil;
import org.adorsys.docusafe.service.BucketService;
import org.adorsys.docusafe.service.impl.BucketServiceImpl;
import org.adorsys.docusafe.service.types.AccessType;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Security;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by peter on 02.02.18 at 22:23.
 */
public class GrantUtilTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(GrantUtilTest.class);
    public final static ExtendedStoreConnection extendedStoreConnection = ExtendedStoreConnectionFactory.get();

    public static Set<UserID> users = new HashSet<>();

    @Before
    public void before() {
        LOGGER.debug("add bouncy castle provider");
        Security.addProvider(new BouncyCastleProvider());
        users.clear();
    }

    @After
    public void after() {
        try {
            users.forEach(userID -> {
                LOGGER.debug("AFTER TEST DESTROY " + userID.getValue());
                BucketService service = new BucketServiceImpl(extendedStoreConnection);
                service.destroyBucket(UserIDUtil.getUserRootBucketDirectory(userID));
            });
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }


    @Test
    public void test1() {
        UserID owner = new UserID("peter");
        UserID receiver = new UserID("affe");

        BucketService bucketService = new BucketServiceImpl(BusinessTest.extendedStoreConnection);
        bucketService.createBucket(UserIDUtil.getHomeBucketDirectory(owner));
        users.add(owner);
        users.add(receiver);
        BucketDirectory documentDirectory = new BucketDirectory("affe/1/2/3");
        GrantUtil.saveBucketGrantFile(bucketService, documentDirectory, owner, receiver, AccessType.WRITE);
        AccessType a = GrantUtil.getAccessTypeOfBucketGrantFile(bucketService, documentDirectory, owner, receiver);
        Assert.assertEquals("accessType ", AccessType.WRITE, a);
    }

    @Test
    public void test2() {
        UserID owner = new UserID("peter");
        UserID receiver = new UserID("affe");


        BucketDirectory documentDirectory = new BucketDirectory(new BucketPath("affe/1/2/3"));
        BucketService bucketService = new BucketServiceImpl(BusinessTest.extendedStoreConnection);
        bucketService.createBucket(UserIDUtil.getHomeBucketDirectory(owner));
        users.add(owner);
        users.add(receiver);

        AccessType a = GrantUtil.getAccessTypeOfBucketGrantFile(bucketService, documentDirectory, owner, receiver);
        Assert.assertEquals("accessType ", AccessType.NONE, a);

        GrantUtil.saveBucketGrantFile(bucketService, documentDirectory, owner, receiver, AccessType.WRITE);
        a = GrantUtil.getAccessTypeOfBucketGrantFile(bucketService, documentDirectory, owner, receiver);
        Assert.assertEquals("accessType ", AccessType.WRITE, a);

        GrantUtil.saveBucketGrantFile(bucketService, documentDirectory, owner, receiver, AccessType.READ);
//        AccessType a;
        a = GrantUtil.getAccessTypeOfBucketGrantFile(bucketService, documentDirectory, owner, receiver);
        Assert.assertEquals("accessType ", AccessType.READ, a);

        BucketPath grantFile = UserIDUtil.getGrantBucketDirectory(owner).append(documentDirectory).addSuffix(GrantUtil.GRANT_EXT);

        Assert.assertTrue("grant file exists", bucketService.fileExists(grantFile));

        GrantUtil.saveBucketGrantFile(bucketService, documentDirectory, owner, receiver, AccessType.NONE);
        a = GrantUtil.getAccessTypeOfBucketGrantFile(bucketService, documentDirectory, owner, receiver);
        Assert.assertEquals("accessType ", AccessType.NONE, a);

        Assert.assertFalse("grant file must not exist any more", bucketService.fileExists(grantFile));
    }

    @Test
    public void test3() {
        UserID owner = new UserID("peter");
        UserID receiver = new UserID("affe");

        BucketService bucketService = new BucketServiceImpl(BusinessTest.extendedStoreConnection);
        bucketService.createBucket(UserIDUtil.getHomeBucketDirectory(owner));
        users.add(owner);
        users.add(receiver);

        BucketDirectory documentDirectory = new BucketDirectory(new BucketPath("affe/1/2/3"));


        AccessType a = GrantUtil.getAccessTypeOfBucketGrantFile(bucketService, documentDirectory, owner, receiver);
        Assert.assertEquals("accessType ", AccessType.NONE, a);

        GrantUtil.saveBucketGrantFile(bucketService, documentDirectory, owner, receiver, AccessType.NONE);
        a = GrantUtil.getAccessTypeOfBucketGrantFile(bucketService, documentDirectory, owner, receiver);
        Assert.assertEquals("accessType ", AccessType.NONE, a);

        BucketPath grantFile = UserIDUtil.getGrantBucketDirectory(owner).append(documentDirectory).addSuffix(GrantUtil.GRANT_EXT);
        Assert.assertFalse("grant file must not exist any more", bucketService.fileExists(grantFile));
    }

}
