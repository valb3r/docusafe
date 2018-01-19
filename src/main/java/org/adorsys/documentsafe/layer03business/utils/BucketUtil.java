package org.adorsys.documentsafe.layer03business.utils;

import org.adorsys.documentsafe.layer00common.exceptions.BaseException;
import org.adorsys.documentsafe.layer01persistence.types.BucketName;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.KeyStoreBucketPath;
import org.adorsys.documentsafe.layer03business.types.UserID;
import org.adorsys.documentsafe.layer03business.types.complex.HomeBucketPath;

/**
 * Created by peter on 19.01.18 at 16:12.
 */
public class BucketUtil {
    public static KeyStoreBucketPath getKeyStoreBucketPath(UserID userID) {
        BucketPath bucketPath = UserIDUtil.getBucketPath(userID);
        if (bucketPath.getDepth() > 1) {
            throw new BaseException("Programming Error. BucketPath must not be deeper than 1 " + bucketPath.toString());
        }
        KeyStoreBucketPath keyStoreBucketPath = new KeyStoreBucketPath(bucketPath.getFirstBucket().getValue());
        keyStoreBucketPath.sub(new BucketName(".KEYSTORE"));
        return keyStoreBucketPath;
    }

    public static HomeBucketPath getHomeBucketPath(BucketPath bucketPath) {
        if (bucketPath.getDepth() > 1) {
            throw new BaseException("Programming Error. BucketPath must not be deeper than 1 " + bucketPath.toString());
        }
        HomeBucketPath homeBucketPath = new HomeBucketPath(bucketPath.getFirstBucket().getValue());
        homeBucketPath.sub(new BucketName("HOME"));
        return homeBucketPath;
    }

    public static BucketPath getFullBucketPath(BucketPath bucketPathKnownToUser, UserID userID) {
        BucketPath bucketPath = UserIDUtil.getBucketPath(userID);
        HomeBucketPath homeBucketPath = BucketUtil.getHomeBucketPath(bucketPath);
        return new BucketPath(homeBucketPath.getObjectHandlePath() + bucketPathKnownToUser.getObjectHandlePath());
    }

}
