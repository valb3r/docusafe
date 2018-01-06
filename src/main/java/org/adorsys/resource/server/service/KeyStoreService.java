package org.adorsys.resource.server.service;

import de.adorsys.resource.server.keyservice.KeyPairGenerator;
import de.adorsys.resource.server.keyservice.KeyStoreGenerator;
import de.adorsys.resource.server.keyservice.SecretKeyGenerator;
import org.adorsys.jkeygen.keystore.PasswordCallbackUtils;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.ExtendedKeystorePersistence;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreAuth;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreBucketName;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreID;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreLocation;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreType;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;

public class KeyStoreService {

    private ExtendedKeystorePersistence keystorePersistence;
    SecretKeyGenerator secretKeyGenerator;

    public KeyStoreService(ExtendedKeystorePersistence keystorePersistence) {
        super();
        this.keystorePersistence = keystorePersistence;
        secretKeyGenerator = new SecretKeyGenerator("AES", 256);
    }

    public KeyStoreLocation createKeyStore(KeyStoreID keyStoreID,
                                           KeyStoreAuth keyStoreAuth,
                                           KeyStoreBucketName keystoreBucketName) {
        try {
            String keyStoreType = null;
            String serverKeyPairAliasPrefix = keyStoreID.getValue();
            Integer numberOfSignKeyPairs = 5;
            Integer numberOfEncKeyPairs = 5;
            Integer numberOfSecretKeys = 5;
            String keyStorePassword = keyStoreID.getValue();
            char[] password = PasswordCallbackUtils.getPassword(keyStoreAuth.getKeypass(), keyStorePassword);
            KeyPairGenerator encKeyPairGenerator = new KeyPairGenerator("RSA", 2048, "SHA256withRSA", "enc-" + keyStoreID.getValue());
            KeyPairGenerator signKeyPairGenerator = new KeyPairGenerator("RSA", 2048, "SHA256withRSA", "sign-" + keyStoreID.getValue());
            KeyStoreGenerator keyStoreGenerator = new KeyStoreGenerator(encKeyPairGenerator, signKeyPairGenerator,
                    secretKeyGenerator, keyStoreType, serverKeyPairAliasPrefix, numberOfSignKeyPairs, numberOfEncKeyPairs,
                    numberOfSecretKeys, new String(password));
            KeyStore userKeyStore = keyStoreGenerator.generate();

            KeyStoreLocation keyStoreLocation = new KeyStoreLocation(keystoreBucketName, keyStoreID, new KeyStoreType(userKeyStore.getType()));
			keystorePersistence.saveKeyStore(userKeyStore, keyStoreAuth.getUserpass(), keyStoreLocation);
			return keyStoreLocation;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
    
    public KeyStore loadKeystore(KeyStoreLocation keyStoreLocation, CallbackHandler userKeystoreHandler){
    	return keystorePersistence.loadKeystore(keyStoreLocation, userKeystoreHandler);
    }
}
