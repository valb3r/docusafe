package org.adorsys.documentsafe.layer02service.serializer;

import org.adorsys.documentsafe.layer02service.types.DocumentKey;
import org.adorsys.documentsafe.layer02service.utils.KeystoreAdapter;
import org.adorsys.encobject.types.KeyStoreType;
import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;

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
    // TODO besser wäre wirklich null zu übergeben, da es nicht gebraucht werden darf
    // private static CallbackHandler keystoreHandler = null;
    private static CallbackHandler keystoreHandler = new PasswordCallbackHandler("just-to-have-a-not-null-callbackhandler".toCharArray());

    /*
     * Deserializes the secret key. In order not to define a proper key
     * serialization format, we reuse the keystore format system.
     */
    public DocumentKey deserializeSecretKey(byte[] decryptedGuardBytes, KeyStoreType keyStoreType) {
        KeyStore secretKeystore = KeystoreAdapter.loadKeystoreFromBytes(decryptedGuardBytes, keystoreHandler, keyStoreType);
        SecretKey secretKey = (SecretKey) KeystoreAdapter.readKeyFromKeystore(secretKeystore, DGS01_KEYID, keystoreHandler);
        return new DocumentKey(secretKey);
    }

    public byte[] serializeSecretKey(DocumentKey documentKey, KeyStoreType keyStoreType) {
        KeyStore docKeyStore = KeystoreAdapter.wrapSecretKey2KeyStore(documentKey.getSecretKey(), DGS01_KEYID, keystoreHandler, keyStoreType);
        return KeystoreAdapter.toBytes(docKeyStore, DGS01_KEYID, keystoreHandler);
    }

    @Override
    public String getSerializerID() {
        return SERIALIZER_ID;
    }
}
