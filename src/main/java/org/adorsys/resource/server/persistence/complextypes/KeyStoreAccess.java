package org.adorsys.resource.server.persistence.complextypes;

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
}
