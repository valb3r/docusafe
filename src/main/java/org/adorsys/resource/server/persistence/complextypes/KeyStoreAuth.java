package org.adorsys.resource.server.persistence.complextypes;

import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;
import org.adorsys.resource.server.exceptions.BaseException;

import javax.security.auth.callback.CallbackHandler;

/**
 * Created by peter on 05.01.18.
 */
public class KeyStoreAuth {
    private final CallbackHandler userpass;
    private CallbackHandler keypass;
    private final String userpassstring;
    private final String keypassstring;
    public KeyStoreAuth(String userPasswordForKeyStore, String keyPasswordForKeyStore) {
        userpass = new PasswordCallbackHandler(userPasswordForKeyStore.toCharArray());
        keypass = new PasswordCallbackHandler(keyPasswordForKeyStore.toCharArray());
        userpassstring = userPasswordForKeyStore;
        keypassstring = keyPasswordForKeyStore;
    }

    public CallbackHandler getUserpass() {
        return userpass;
    }

    public CallbackHandler getKeypass() {
        if (keypass == null) {
            throw new BaseException("Access to KeyPass not allowed");
        }
        return keypass;
    }

    public String getUserpassString() {
        return userpassstring;
    }

    public String getKeypassString() {
        return keypassstring;
    }

    // ToDo delete this test method
    public void setEmptyKeyPass() {
        keypass = null;
    }
}
