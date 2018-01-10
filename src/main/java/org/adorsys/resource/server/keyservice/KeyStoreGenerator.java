package org.adorsys.resource.server.keyservice;

import org.adorsys.jkeygen.keystore.KeyPairData;
import org.adorsys.jkeygen.keystore.KeystoreBuilder;
import org.adorsys.jkeygen.keystore.SecretKeyData;
import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreID;
import org.adorsys.resource.server.persistence.basetypes.ReadKeyPassword;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreCreationConfig;
import org.apache.commons.lang3.RandomStringUtils;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;
import java.util.UUID;

public class KeyStoreGenerator {
    private final String keyStoreType;
    private final String serverKeyPairAliasPrefix;
    private final CallbackHandler readKeyHandler;
    private final KeyStoreID keyStoreID;
    private final KeyStoreCreationConfig config;

    public KeyStoreGenerator(
            KeyStoreCreationConfig config,
            KeyStoreID keyStoreID,
            String keyStoreType,
            String serverKeyPairAliasPrefix,
            ReadKeyPassword readKeyPassword
    ) {
        this.config = config;
        this.keyStoreID = keyStoreID;
        this.keyStoreType = keyStoreType;
        this.serverKeyPairAliasPrefix = serverKeyPairAliasPrefix;
        this.readKeyHandler = new PasswordCallbackHandler(readKeyPassword.getValue().toCharArray());
    }

    public KeyStore generate() {
        try {
            KeystoreBuilder keystoreBuilder = new KeystoreBuilder().withStoreType(keyStoreType);
            PasswordCallbackHandler dummyKeyHandler = new PasswordCallbackHandler("".toCharArray());

            {
                KeyPairGenerator encKeyPairGenerator = config.getEncKeyPairGenerator(keyStoreID);
                int numberOfEncKeyPairs = config.getEncKeyNumber();
                for (int i = 0; i < numberOfEncKeyPairs; i++) {
                    KeyPairData signatureKeyPair = encKeyPairGenerator.generateEncryptionKey(
                            serverKeyPairAliasPrefix + RandomStringUtils.randomAlphanumeric(5).toUpperCase(),
                            readKeyHandler
                    );

                    keystoreBuilder = keystoreBuilder.withKeyEntry(signatureKeyPair);
                }
            }
            {
                // TODO warum hier einen UUID Key und in den anderen nur 5 stelliger string
                KeyPairGenerator signKeyPairGenerator = config.getSignKeyPairGenerator(keyStoreID);
                int numberOfSignKeyPairs = config.getSignKeyNumber();
                for (int i = 0; i < numberOfSignKeyPairs; i++) {
                    KeyPairData signatureKeyPair = signKeyPairGenerator.generateSignatureKey(
                            serverKeyPairAliasPrefix + UUID.randomUUID().toString(),
                            readKeyHandler
                    );

                    keystoreBuilder = keystoreBuilder.withKeyEntry(signatureKeyPair);
                }
            }
            {
                SecretKeyGenerator secretKeyGenerator = config.getSecretKeyGenerator(keyStoreID);
                int numberOfSecretKeys = config.getSecretKeyNumber();
                for (int i = 0; i < numberOfSecretKeys; i++) {
                    SecretKeyData secretKeyData = secretKeyGenerator.generate(
                            serverKeyPairAliasPrefix + RandomStringUtils.randomAlphanumeric(5).toUpperCase(),
                            readKeyHandler
                    );

                    keystoreBuilder = keystoreBuilder.withKeyEntry(secretKeyData);
                }
            }
            return keystoreBuilder.build();
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
