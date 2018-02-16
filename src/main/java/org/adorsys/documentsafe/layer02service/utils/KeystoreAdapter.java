package org.adorsys.documentsafe.layer02service.utils;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.encobject.types.KeyStoreType;
import org.adorsys.jkeygen.keystore.KeyStoreService;
import org.adorsys.jkeygen.keystore.KeystoreBuilder;
import org.adorsys.jkeygen.keystore.PasswordCallbackUtils;
import org.adorsys.jkeygen.keystore.SecretKeyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import javax.security.auth.callback.CallbackHandler;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Keystore utilities.
 *
 * @author fpo
 */
public class KeystoreAdapter {
    private final static Logger LOGGER = LoggerFactory.getLogger(KeystoreAdapter.class);

    /**
     * Wrap a SecretKey into a keystore.
     *
     * @param secretKey
     * @param alias
     * @param keyPassHandler
     * @return
     */
    public static KeyStore wrapSecretKey2KeyStore(SecretKey secretKey, String alias, CallbackHandler keyPassHandler, KeyStoreType keyStoreType) {
        SecretKeyData secretKeyData = SecretKeyData.builder().secretKey(secretKey).alias(alias).passwordSource(keyPassHandler).build();
        try {
            return new KeystoreBuilder().withKeyEntry(secretKeyData).withStoreType(keyStoreType.getValue()).build();
        } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    /**
     * Load keystore from bytes, using the format defined in /encobject/src/main/protobuf/KeystorePbf.proto
     *
     */
    public static KeyStore loadKeystoreFromBytes(byte[] keyStoreDataBytes, CallbackHandler keystoreHandler, KeyStoreType keyStoreType) {
        try {
            String storeid = "if you can see this, its a programming error in KeyStoreAdapter";
            return KeyStoreService.loadKeyStore(keyStoreDataBytes, storeid, keyStoreType.getValue(), keystoreHandler);
        } catch (Exception ex) {
            throw BaseExceptionHandler.handle(ex);
        }
    }

    /**
     * Retrieves the key with the given keyID from the keystore. The key password will be retrieved by
     * calling the keyPassHandler passing the keyId.
     */
    public static Key readKeyFromKeystore(KeyStore keyStore, String keyID, CallbackHandler keyPassHandler) {
        try {
            return keyStore.getKey(keyID, PasswordCallbackUtils.getPassword(keyPassHandler, keyID));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public static byte[] toBytes(KeyStore keystore, String storeid, CallbackHandler storePassHandler) {
        try {
            byte[] bs = KeyStoreService.toByteArray(keystore, storeid, storePassHandler);
            return bs;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
