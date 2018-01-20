package org.adorsys.documentsafe.layer02service.types.complextypes;

import org.adorsys.documentsafe.layer01persistence.types.complextypes.KeyStoreLocation;

/**
 * Created by peter on 08.01.18.
 */
public class KeyStoreAccess {
    private final KeyStoreLocation keyStoreLocation;
    private final KeyStoreAuth keyStoreAuth;

    public KeyStoreAccess(KeyStoreLocation keyStoreLocation, KeyStoreAuth keyStoreAuth) {
        this.keyStoreLocation = keyStoreLocation;
        this.keyStoreAuth = keyStoreAuth;
    }

    public KeyStoreAuth getKeyStoreAuth() {
        return keyStoreAuth;
    }

    public KeyStoreLocation getKeyStoreLocation() {

        return keyStoreLocation;
    }

    @Override
    public String toString() {
        return "KeyStoreAccess{" +
                "keyStoreLocation=" + keyStoreLocation +
                ", keyStoreAuth=" + keyStoreAuth +
                '}';
    }
}
