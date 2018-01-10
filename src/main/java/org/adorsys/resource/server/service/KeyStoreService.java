package org.adorsys.resource.server.service;

import de.adorsys.resource.server.keyservice.KeyStoreGenerator;
import de.adorsys.resource.server.keyservice.SecretKeyGenerator;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.ExtendedKeystorePersistence;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreBucketName;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreID;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreType;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreAuth;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreCreationConfig;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;

public class KeyStoreService {
    private final static Logger LOGGER = LoggerFactory.getLogger(KeyStoreService.class);

    private ExtendedKeystorePersistence keystorePersistence;
    SecretKeyGenerator secretKeyGenerator;

    public KeyStoreService(ExtendedKeystorePersistence keystorePersistence) {
        super();
        this.keystorePersistence = keystorePersistence;
    }

    /**
     *
     * @param keyStoreID
     * @param keyStoreAuth
     * @param keystoreBucketName
     * @param config may be null
     * @return
     */
    public KeyStoreLocation createKeyStore(KeyStoreID keyStoreID,
                                           KeyStoreAuth keyStoreAuth,
                                           KeyStoreBucketName keystoreBucketName,
                                           KeyStoreCreationConfig config) {
        try {
            LOGGER.info("start create keystore " + keyStoreID);
            if (config == null ) {
                config = new KeyStoreCreationConfig(5,5,5);
            }
            String keyStoreType = null;
            String serverKeyPairAliasPrefix = keyStoreID.getValue();
            KeyStoreGenerator keyStoreGenerator = new KeyStoreGenerator(
                    config,
                    keyStoreID,
                    keyStoreType,
                    serverKeyPairAliasPrefix,
                    keyStoreAuth.getReadKeyPassword());
            KeyStore userKeyStore = keyStoreGenerator.generate();

            KeyStoreLocation keyStoreLocation = new KeyStoreLocation(keystoreBucketName, keyStoreID, new KeyStoreType(userKeyStore.getType()));
			keystorePersistence.saveKeyStore(userKeyStore, keyStoreAuth.getReadStoreHandler(), keyStoreLocation);
            LOGGER.info("finished create keystore " + keyStoreID + " @ " + keyStoreLocation);
			return keyStoreLocation;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
    
    public KeyStore loadKeystore(KeyStoreLocation keyStoreLocation, CallbackHandler userKeystoreHandler){
        LOGGER.info("start load keystore @ " + keyStoreLocation);
        KeyStore keyStore = keystorePersistence.loadKeystore(keyStoreLocation, userKeystoreHandler);
        LOGGER.info("finished load keystore @ " + keyStoreLocation);
        return keyStore;
    }
}
