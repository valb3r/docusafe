package org.adorsys.resource.server.persistence.complextypes;

import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;
import org.adorsys.resource.server.exceptions.BaseException;
import org.adorsys.resource.server.persistence.basetypes.ReadKeyPassword;
import org.adorsys.resource.server.persistence.basetypes.ReadStorePassword;

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
            throw new BaseException("Access to READ STORE HANDLER not allowed");
        }
        return new PasswordCallbackHandler(readStorePassword.getValue().toCharArray());
    }

    public CallbackHandler getReadKeyHandler() {
        if (readKeyPassword == null) {
            throw new BaseException("Access to READ KEY HANDLER not allowed");
        }
        return new PasswordCallbackHandler(readKeyPassword.getValue().toCharArray());
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
    }

    public void deleteReadStorePassword() {
        readStorePassword = null;
    }
}
