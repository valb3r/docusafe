package org.adorsys.resource.server.persistence.complextypes;

import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;
import org.adorsys.resource.server.exceptions.BaseException;
import org.adorsys.resource.server.persistence.basetypes.ReadKeyPassword;
import org.adorsys.resource.server.persistence.basetypes.ReadStorePassword;

import javax.security.auth.callback.CallbackHandler;

/**
 * Created by peter on 05.01.18.
 */
public class KeyStoreAuth {
    private CallbackHandler readStoreHandler;
    private CallbackHandler readKeyHandler;
    private ReadStorePassword readStorePassword;
    private ReadKeyPassword readKeyPassword;

    public KeyStoreAuth(ReadStorePassword readStorePassword, ReadKeyPassword readKeyPassword) {
        this.readStorePassword = readStorePassword;
        this.readKeyPassword = readKeyPassword;
        this.readStoreHandler = this.readStorePassword != null ? new PasswordCallbackHandler(readStorePassword.getValue().toCharArray()) : null;
        this.readKeyHandler = this.readKeyPassword != null ? new PasswordCallbackHandler(readKeyPassword.getValue().toCharArray()) : null;
    }

    public CallbackHandler getReadStoreHandler() {
        if (readStoreHandler == null) {
            throw new BaseException("Access to READ STORE HANDLER not allowed");
        }
        return readStoreHandler;
    }

    public CallbackHandler getReadKeyHandler() {
        if (readKeyHandler == null) {
            throw new BaseException("Access to READ KEY HANDLER not allowed");
        }
        return readKeyHandler;
    }

    public ReadStorePassword getReadStorePassword() {
        if (readStorePassword == null) {
            throw new BaseException("Access to READ STORE PASSWORD not allowed");
        }
        return readStorePassword;
    }

    public ReadKeyPassword getReadKeyPassword() {
        if (readKeyPassword == null) {
            throw new BaseException("Access to READ KEY PASSWORD not allowed");
        }
        return readKeyPassword;
    }

    public void deleteReadKeyPassword() {
        readKeyPassword = null;
        readKeyHandler = null;
    }

    public void deleteReadStorePassword() {
        readStorePassword = null;
        readStoreHandler = null;
    }
}
