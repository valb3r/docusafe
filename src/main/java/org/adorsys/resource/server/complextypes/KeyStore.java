package org.adorsys.resource.server.complextypes;

import org.adorsys.resource.server.basetypes.KeyID;
import org.adorsys.resource.server.basetypes.KeyStoreName;
import org.adorsys.resource.server.basetypes.UserID;
import org.adorsys.resource.server.basetypes.UserKey;
import org.adorsys.resource.server.basetypes.UserPublicKey;
import org.adorsys.resource.server.basetypes.UserSecretKey;
import org.adorsys.resource.server.exceptions.ServiceException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by peter on 23.12.17 at 17:50.
 */
public class KeyStore {
    public static KeyStoreName getKeyStoreName(UserID userID) {
        return new KeyStoreName(userID.getValue() + ".keystore");
    }

    public KeyStore(KeyStoreName keyStoreName, byte[] rawContent) {
        throw new ServiceException("NYI");
    }

    KeyStoreName keyStoreName;


    Map<KeyID, UserKey> privateKeyMap = new HashMap<>();
    Map<KeyID, UserPublicKey> publicKeyMap = new HashMap<>();
    Map<KeyID, UserSecretKey> secretKeyKeyMapKeyMap = new HashMap<>();


}
