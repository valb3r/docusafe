package org.adorsys.documentsafe.layer03business.utils;

import org.adorsys.documentsafe.layer03business.types.UserID;
import org.adorsys.documentsafe.layer03business.types.complex.UserIDAuth;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.KeyStoreAuth;
import org.adorsys.encobject.domain.ReadStorePassword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 19.01.18 at 15:31.
 */
public class UserIDUtil {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserIDUtil.class);
    public static final String KEY_STORE_TYPE = "KeyStoreType";
    private final static String DEFAULT_KEYSTORE_TYPE = "UBER";

    public static BucketDirectory getUserRootBucketDirectory(UserID userID) {
        return new BucketDirectory("BP-" + userID.getValue());
    }

    public static BucketPath getKeyStorePath(UserID userID) {
        return getKeyStoreDirectory(userID).appendName("KS-" + userID.getValue());
    }

    public static KeyStoreAuth getKeyStoreAuth(UserIDAuth userIDAuth) {
        return new KeyStoreAuth(getReadStorePassword(userIDAuth.getUserID()), userIDAuth.getReadKeyPassword());
    }

    public static BucketDirectory getKeyStoreDirectory(UserID userID) {
        return UserIDUtil.getUserRootBucketDirectory(userID).appendDirectory(".KEYSTORE");
    }

    public static BucketDirectory getHomeBucketDirectory(UserID userID) {
        return UserIDUtil.getUserRootBucketDirectory(userID).appendDirectory("HOME");
    }

    public static BucketDirectory getGrantBucketDirectory(UserID userID) {
        return UserIDUtil.getUserRootBucketDirectory(userID).appendDirectory(".GRANTS");
    }

    private static ReadStorePassword getReadStorePassword(UserID userID) {
        return new ReadStorePassword("ReadStorePasswordFor" + userID.getValue());
    }

}
