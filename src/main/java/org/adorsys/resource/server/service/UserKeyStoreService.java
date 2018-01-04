package org.adorsys.resource.server.service;

import java.security.KeyStore;

import javax.security.auth.callback.CallbackHandler;

import org.adorsys.jkeygen.keystore.PasswordCallbackUtils;
import org.adorsys.resource.server.basetypes.UserID;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.ExtendedKeystorePersistence;
import org.adorsys.resource.server.persistence.basetypes.BucketName;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreID;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreName;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreType;

import de.adorsys.resource.server.keyservice.KeyPairGenerator;
import de.adorsys.resource.server.keyservice.KeyStoreGenerator;
import de.adorsys.resource.server.keyservice.SecretKeyGenerator;

public class UserKeyStoreService {

    private ExtendedKeystorePersistence keystorePersistence;
    SecretKeyGenerator secretKeyGenerator;

    public UserKeyStoreService(ExtendedKeystorePersistence keystorePersistence) {
        super();
        this.keystorePersistence = keystorePersistence;
        secretKeyGenerator = new SecretKeyGenerator("AES", 256);
    }

    public KeyStore createUserKeyStore(UserID userId, CallbackHandler userKeystoreHandler, CallbackHandler keyPassHandler,
                                       BucketName bucketName) {
        try {
            String keyStoreType = null;
            String serverKeyPairAliasPrefix = userId.getValue();
            Integer numberOfSignKeyPairs = 5;
            Integer numberOfEncKeyPairs = 5;
            Integer numberOfSecretKeys = 5;
            String keyStorePassword = userId.getValue();
            char[] password = PasswordCallbackUtils.getPassword(keyPassHandler, keyStorePassword);
            KeyPairGenerator encKeyPairGenerator = new KeyPairGenerator("RSA", 2048, "SHA256withRSA", "enc-" + userId.getValue());
            KeyPairGenerator signKeyPairGenerator = new KeyPairGenerator("RSA", 2048, "SHA256withRSA", "sign-" + userId.getValue());
            KeyStoreGenerator keyStoreGenerator = new KeyStoreGenerator(encKeyPairGenerator, signKeyPairGenerator,
                    secretKeyGenerator, keyStoreType, serverKeyPairAliasPrefix, numberOfSignKeyPairs, numberOfEncKeyPairs,
                    numberOfSecretKeys, new String(password));
            KeyStore userKeyStore = keyStoreGenerator.generate();
            
            KeyStoreName keyStoreName = new KeyStoreName(bucketName, new KeyStoreID(userId.getValue()), new KeyStoreType(userKeyStore.getType()));
			keystorePersistence.saveKeyStore(userKeyStore, userKeystoreHandler, keyStoreName);
			return userKeyStore;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
