package org.adorsys.resource.server.complextypes;

import org.adorsys.resource.server.basetypes.*;
import org.adorsys.resource.server.basetypes.DocumnentKeyID;
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


    Map<DocumnentKeyID, UserKey> privateKeyMap = new HashMap<>();
    Map<DocumnentKeyID, UserPublicKey> publicKeyMap = new HashMap<>();
    Map<DocumnentKeyID, UserSecretKey> secretKeyKeyMapKeyMap = new HashMap<>();


}
