package org.adorsys.documentsafe.layer03business.utils;

import org.adorsys.documentsafe.layer00common.exceptions.BaseException;
import org.adorsys.documentsafe.layer01persistence.types.BucketName;
import org.adorsys.documentsafe.layer01persistence.types.KeyStoreID;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.KeyStoreBucketPath;
import org.adorsys.documentsafe.layer02service.types.ReadStorePassword;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAuth;
import org.adorsys.documentsafe.layer03business.types.UserID;
import org.adorsys.documentsafe.layer03business.types.complex.HomeBucketPath;
import org.adorsys.documentsafe.layer03business.types.complex.UserIDAuth;

/**
 * Created by peter on 19.01.18 at 15:31.
 */
public class UserIDUtil {
    public static BucketPath getBucketPath(UserID userID) {
        return new BucketPath("BP-" + userID.getValue());
    }


    public static KeyStoreID getKeyStoreID(UserID userID) {
        return new KeyStoreID("KS-" + userID.getValue());
    }

    public static KeyStoreAuth getKeyStoreAuth(UserIDAuth userIDAuth) {
        return new KeyStoreAuth(getReadStorePassword(userIDAuth.getUserID()), userIDAuth.getReadKeyPassword());
    }

    private static ReadStorePassword getReadStorePassword(UserID userID) {
        return new ReadStorePassword("ReadStorePasswordFor" + userID.getValue());
    }
}
