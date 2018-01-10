package org.adorsys.documentsafe.layer02service.serializer;

import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;
import org.adorsys.documentsafe.layer00common.basetypes.DocumentKey;
import org.adorsys.documentsafe.layer02service.utils.KeystoreAdapter;

import javax.crypto.SecretKey;
import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;

public class DocumentGuardSerializer01 implements DocumentGuardSerializer {

    /**
     * This is the proprietary format for the serialization of docu guard. In order to allow for modification or
     * migration of serialization format, format-name must be used as file extension.
     */
    public static final String SERIALIZER_ID = "dgs01";
    private static final String DGS01_KEYID = "keyid";
    private static CallbackHandler keystoreHandler = new PasswordCallbackHandler("any-password".toCharArray());

    /*
     * Deserializes the secret key. In order not to define a proper key
     * serialization format, we reuse the keystore format system.
     */
    public DocumentKey deserializeSecretKey(byte[] decryptedGuardBytes) {
        KeyStore secretKeystore;
        secretKeystore = KeystoreAdapter.loadKeystoreFromBytes(decryptedGuardBytes, DGS01_KEYID, keystoreHandler);
        SecretKey secretKey = (SecretKey) KeystoreAdapter.readKeyFromKeystore(secretKeystore, DGS01_KEYID, keystoreHandler);
        return new DocumentKey(secretKey);
    }

    public byte[] serializeSecretKey(DocumentKey documentKey) {
        KeyStore docKeyStore = KeystoreAdapter.wrapSecretKey2KeyStore(documentKey.getSecretKey(), DGS01_KEYID, keystoreHandler);
        return KeystoreAdapter.toBytes(docKeyStore, DGS01_KEYID, keystoreHandler);
    }
}
