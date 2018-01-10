package org.adorsys.documentsafe.layer02service;

import org.adorsys.documentsafe.layer00common.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer02service.generators.KeyStoreGenerator;
import org.adorsys.documentsafe.layer02service.generators.SecretKeyGenerator;
import org.adorsys.documentsafe.layer01persistence.types.KeyStoreBucketName;
import org.adorsys.documentsafe.layer01persistence.types.KeyStoreType;
import org.adorsys.documentsafe.layer02service.generators.KeyStoreCreationConfig;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.KeyStoreLocation;
import org.adorsys.documentsafe.layer01persistence.ExtendedKeystorePersistence;
import org.adorsys.documentsafe.layer01persistence.types.KeyStoreID;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAuth;
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
