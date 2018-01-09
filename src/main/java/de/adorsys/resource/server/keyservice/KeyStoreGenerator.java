package de.adorsys.resource.server.keyservice;

import org.adorsys.jkeygen.keystore.KeyPairData;
import org.adorsys.jkeygen.keystore.KeystoreBuilder;
import org.adorsys.jkeygen.keystore.SecretKeyData;
import org.adorsys.jkeygen.pwd.PasswordCallbackHandler;
import org.adorsys.resource.server.persistence.basetypes.ReadKeyPassword;
import org.apache.commons.lang3.RandomStringUtils;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;
import java.util.UUID;

public class KeyStoreGenerator {

    private final KeyPairGenerator encKeyPairGenerator;
    private final Integer numberOfEncKeyPairs;
    private final KeyPairGenerator signKeyPairGenerator;
    private final Integer numberOfSignKeyPairs;
    private final SecretKeyGenerator secretKeyGenerator;
    private final Integer numberOfSecretKeys;

    private final String keyStoreType;
    private final String serverKeyPairAliasPrefix;
    private final CallbackHandler readKeyHandler;

    public KeyStoreGenerator(
            KeyPairGenerator encKeyPairGenerator,
            Integer numberOfEncKeyPairs,
            KeyPairGenerator signKeyPairGenerator,
            Integer numberOfSignKeyPairs,
            SecretKeyGenerator secretKeyGenerator,
            Integer numberOfSecretKeys,
            String keyStoreType,
            String serverKeyPairAliasPrefix,
            ReadKeyPassword readKeyPassword
    ) {
        this.encKeyPairGenerator = encKeyPairGenerator;
        this.numberOfEncKeyPairs = numberOfEncKeyPairs;
        this.signKeyPairGenerator = signKeyPairGenerator;
        this.numberOfSignKeyPairs = numberOfSignKeyPairs;
        this.secretKeyGenerator = secretKeyGenerator;
        this.numberOfSecretKeys = numberOfSecretKeys;

        this.keyStoreType = keyStoreType;
        this.serverKeyPairAliasPrefix = serverKeyPairAliasPrefix;

        readKeyHandler = new PasswordCallbackHandler(readKeyPassword.getValue().toCharArray());
    }
    
    public KeyStore generate() {
        try {
            KeystoreBuilder keystoreBuilder = new KeystoreBuilder().withStoreType(keyStoreType);
            for (int i = 0; i < numberOfSignKeyPairs; i++) {
                KeyPairData signatureKeyPair = signKeyPairGenerator.generateSignatureKey(
                        serverKeyPairAliasPrefix + UUID.randomUUID().toString(),
                        readKeyHandler
                );

                keystoreBuilder = keystoreBuilder.withKeyEntry(signatureKeyPair);
            }
            for (int i = 0; i < numberOfEncKeyPairs; i++) {
                KeyPairData signatureKeyPair = encKeyPairGenerator.generateEncryptionKey(
                        serverKeyPairAliasPrefix + RandomStringUtils.randomAlphanumeric(5).toUpperCase(),
                        readKeyHandler
                );

                keystoreBuilder = keystoreBuilder.withKeyEntry(signatureKeyPair);
            }
            for (int i = 0; i < numberOfSecretKeys; i++) {
                SecretKeyData secretKeyData = secretKeyGenerator.generate(
                        serverKeyPairAliasPrefix + RandomStringUtils.randomAlphanumeric(5).toUpperCase(),
                        readKeyHandler
                );

                keystoreBuilder = keystoreBuilder.withKeyEntry(secretKeyData);
            }

            return keystoreBuilder.build();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
