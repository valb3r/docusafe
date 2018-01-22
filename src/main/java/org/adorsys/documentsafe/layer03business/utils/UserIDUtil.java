package org.adorsys.documentsafe.layer03business.utils;

import org.adorsys.documentsafe.layer00common.exceptions.BaseException;
import org.adorsys.documentsafe.layer01persistence.types.BucketName;
import org.adorsys.documentsafe.layer01persistence.types.KeyStoreID;
import org.adorsys.documentsafe.layer01persistence.types.KeyStoreType;
import org.adorsys.documentsafe.layer01persistence.types.ListRecursiveFlag;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.KeyStoreBucketPath;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.KeyStoreLocation;
import org.adorsys.documentsafe.layer02service.BucketService;
import org.adorsys.documentsafe.layer02service.types.PlainFileContent;
import org.adorsys.documentsafe.layer02service.types.PlainFileName;
import org.adorsys.documentsafe.layer02service.types.ReadStorePassword;
import org.adorsys.documentsafe.layer02service.types.complextypes.BucketContent;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAuth;
import org.adorsys.documentsafe.layer03business.types.UserHomeBucketPath;
import org.adorsys.documentsafe.layer03business.types.UserID;
import org.adorsys.documentsafe.layer03business.types.UserRootBucketPath;
import org.adorsys.documentsafe.layer03business.types.complex.UserIDAuth;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

/**
 * Created by peter on 19.01.18 at 15:31.
 */
public class UserIDUtil {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserIDUtil.class);
    public static final String KEY_STORE_TYPE = "KeyStoreType->";

    public static UserRootBucketPath getUserRootBucketPath(UserID userID) {
        return new UserRootBucketPath("BP-" + userID.getValue());
    }


    public static KeyStoreID getKeyStoreID(UserID userID) {
        return new KeyStoreID("KS-" + userID.getValue());
    }

    public static KeyStoreAuth getKeyStoreAuth(UserIDAuth userIDAuth) {
        return new KeyStoreAuth(getReadStorePassword(userIDAuth.getUserID()), userIDAuth.getReadKeyPassword());
    }


    public static KeyStoreBucketPath getKeyStoreBucketPath(UserID userID) {
        UserRootBucketPath userRootBucketPath = UserIDUtil.getUserRootBucketPath(userID);
        KeyStoreBucketPath keyStoreBucketPath = new KeyStoreBucketPath(userRootBucketPath.getFirstBucket().getValue());
        keyStoreBucketPath.sub(new BucketName(".KEYSTORE"));
        return keyStoreBucketPath;
    }

    public static UserHomeBucketPath getHomeBucketPath(UserID userID) {
        UserRootBucketPath userRootBucketPath = UserIDUtil.getUserRootBucketPath(userID);
        UserHomeBucketPath userHomeBucketPath = new UserHomeBucketPath(userRootBucketPath.getObjectHandlePath());
        userHomeBucketPath.sub(new BucketName("HOME"));
        return userHomeBucketPath;
    }

    private static ReadStorePassword getReadStorePassword(UserID userID) {
        return new ReadStorePassword("ReadStorePasswordFor" + userID.getValue());
    }


    public static void safeKeyStoreType(UserID userID, KeyStoreType keyStoreType, BucketService bucketService) {
        PlainFileName plainFileName = new PlainFileName(KEY_STORE_TYPE + keyStoreType.getValue());
        bucketService.createPlainFile(getKeyStoreBucketPath(userID),
                plainFileName, new PlainFileContent("not encrypted".getBytes()));

    }

    public static KeyStoreLocation getKeyStoreLocation(UserID userID, BucketService bucketService) {
        KeyStoreBucketPath keyStoreBucketPath = getKeyStoreBucketPath(userID);
        KeyStoreID keyStoreID = getKeyStoreID(userID);

        BucketContent bucketContent = bucketService.readDocumentBucket(keyStoreBucketPath, ListRecursiveFlag.FALSE);
        String prefix = KEY_STORE_TYPE;

        for (StorageMetadata meta : bucketContent.getStrippedContent()) {
            if (meta.getName().startsWith(prefix)) {
                KeyStoreType keyStoreType = new KeyStoreType(meta.getName().substring(prefix.length()));
                return new KeyStoreLocation(keyStoreBucketPath, keyStoreID, keyStoreType);
            }
        }
        throw new BaseException("Did not find stored keyStoreType in " + keyStoreBucketPath);
    }

}
