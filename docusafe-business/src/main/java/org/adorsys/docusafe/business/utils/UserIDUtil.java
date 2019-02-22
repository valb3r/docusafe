package org.adorsys.docusafe.business.utils;

import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
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

    public static BucketDirectory getUserRootBucketDirectory(UserID userID) {
        return new BucketDirectory("bp-" + userID.getValue().toLowerCase());
    }

    public static BucketPath getKeyStorePath(UserID userID) {
        return getKeyStoreDirectory(userID).appendName("KS-" + userID.getValue());
    }

    public static KeyStoreAuth getKeyStoreAuth(UserIDAuth userIDAuth) {
        return new KeyStoreAuth(getReadStorePassword(userIDAuth.getUserID()), userIDAuth.getReadKeyPassword());
    }

    public static BucketDirectory getKeyStoreDirectory(UserID userID) {
        return UserIDUtil.getUserRootBucketDirectory(userID).appendDirectory(".keys");
    }

    public static BucketDirectory getHomeBucketDirectory(UserID userID) {
        return UserIDUtil.getUserRootBucketDirectory(userID).appendDirectory("home");
    }

    public static BucketDirectory getInboxDirectory(UserID userID) {
        return UserIDUtil.getUserRootBucketDirectory(userID).appendDirectory("INBOX");
    }

    private static ReadStorePassword getReadStorePassword(UserID userID) {
        return new ReadStorePassword("ReadStorePasswordFor" + userID.getValue());
    }

}
