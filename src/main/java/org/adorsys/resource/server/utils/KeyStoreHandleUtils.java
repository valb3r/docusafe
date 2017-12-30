package org.adorsys.resource.server.utils;

import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.resource.server.basetypes.BucketName;
import org.adorsys.resource.server.basetypes.KeyStoreName;
import org.adorsys.resource.server.basetypes.UserID;

public class KeyStoreHandleUtils {
    private static KeyStoreName userKeystoreNameFromUserId(UserID userID) {
        // keystore name is user id + keystore
        // TODO : we have a similar idea in the class: org.adorsys.encobject.userdata.UserDataNamingPolicy
        return new KeyStoreName(userID.getValue() + "_Keystore");
    }
    
    public static ObjectHandle userkeyStoreHandle(BucketName bucketName, UserID userID){
        KeyStoreName keystoreName = KeyStoreHandleUtils.userKeystoreNameFromUserId(userID);
        return new ObjectHandle(bucketName.getValue(), keystoreName.getValue());
    }

}
