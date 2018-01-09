package org.adorsys.resource.server.service;

import de.adorsys.resource.server.keyservice.KeyStoreGenerator;
import de.adorsys.resource.server.keyservice.SecretKeyGenerator;
import org.adorsys.jkeygen.keystore.PasswordCallbackUtils;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.ExtendedKeystorePersistence;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreBucketName;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreID;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreType;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreAuth;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreCreationConfig;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreLocation;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;

public class KeyStoreService {

    private ExtendedKeystorePersistence keystorePersistence;
    SecretKeyGenerator secretKeyGenerator;

    public KeyStoreService(ExtendedKeystorePersistence keystorePersistence) {
        super();
        this.keystorePersistence = keystorePersistence;
    }

    public KeyStoreLocation createKeyStore(KeyStoreID keyStoreID,
                                           KeyStoreAuth keyStoreAuth,
                                           KeyStoreBucketName keystoreBucketName) {
        return createKeyStore(keyStoreID, keyStoreAuth, keystoreBucketName, new KeyStoreCreationConfig(5,5,5, keyStoreID));
    }
    public KeyStoreLocation createKeyStore(KeyStoreID keyStoreID,
                                           KeyStoreAuth keyStoreAuth,
                                           KeyStoreBucketName keystoreBucketName,
                                           KeyStoreCreationConfig config) {

        try {
            String keyStoreType = null;
            String serverKeyPairAliasPrefix = keyStoreID.getValue();
            String keyStorePassword = keyStoreID.getValue();
            char[] password = PasswordCallbackUtils.getPassword(keyStoreAuth.getKeypass(), keyStorePassword);
            KeyStoreGenerator keyStoreGenerator = new KeyStoreGenerator(
                    config.getEncKeyPairGenerator(),
                    config.getSignKeyPairGenerator(),
                    config.getSecretKeyGenerator(),
                    keyStoreType,
                    serverKeyPairAliasPrefix,
                    config.getSignKeyNumber(),
                    config.getEncKeyNumber(),
                    config.getSecretKeyNumber(),
                    new String(password));
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
