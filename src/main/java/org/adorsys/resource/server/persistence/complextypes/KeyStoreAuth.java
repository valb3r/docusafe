package org.adorsys.resource.server.persistence.complextypes;

import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;

import javax.security.auth.callback.CallbackHandler;

/**
 * Created by peter on 05.01.18.
 */
public class KeyStoreAuth {
    private CallbackHandler userpass;
    private CallbackHandler keypass;
    public KeyStoreAuth(String userPasswordForKeyStore, String keyPasswordForKeyStore) {
        userpass = new PasswordCallbackHandler(userPasswordForKeyStore.toCharArray());
        keypass = new PasswordCallbackHandler(keyPasswordForKeyStore.toCharArray());
    }

    public CallbackHandler getUserpass() {
        return userpass;
    }

    public CallbackHandler getKeypass() {
        return keypass;
    }
}
