package org.adorsys.documentsafe.layer00common.complextypes;

import org.adorsys.documentsafe.layer00common.exceptions.KeyStoreAuthException;
import org.adorsys.documentsafe.layer00common.basetypes.ReadKeyPassword;
import org.adorsys.documentsafe.layer00common.basetypes.ReadStorePassword;
import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;

import javax.security.auth.callback.CallbackHandler;

/**
 * Created by peter on 05.01.18.
 *
 * BTW, so liest man das Kennwort aus dem Handler
 * char[] password = PasswordCallbackUtils.getPassword(keyStoreAuth.getReadKeyHandler(), keyStorePassword);
 */
public class KeyStoreAuth {
    private ReadStorePassword readStorePassword;
    private ReadKeyPassword readKeyPassword;

    public KeyStoreAuth(ReadStorePassword readStorePassword, ReadKeyPassword readKeyPassword) {
        this.readStorePassword = readStorePassword;
        this.readKeyPassword = readKeyPassword;
    }

    public CallbackHandler getReadStoreHandler() {
        if (readStorePassword == null) {
            throw new KeyStoreAuthException("Access to READ STORE HANDLER not allowed.");
        }
        return new PasswordCallbackHandler(readStorePassword.getValue().toCharArray());
    }

    public CallbackHandler getReadKeyHandler() {
        if (readKeyPassword == null) {
            throw new KeyStoreAuthException("Access to READ KEY HANDLER not allowed.");
        }
        return new PasswordCallbackHandler(readKeyPassword.getValue().toCharArray());
    }

    public ReadKeyPassword getReadKeyPassword() {
        if (readKeyPassword == null) {
            throw new KeyStoreAuthException("Access to READ KEY PASSWORD not allowed");
        }
        return readKeyPassword;
    }

    public void setReadKeyPassword(ReadKeyPassword readKeyPassword) {
        this.readKeyPassword = readKeyPassword;
    }
}