package org.adorsys.documentsafe.layer02service.impl;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.utils.HexUtil;
import org.adorsys.documentsafe.layer02service.BucketService;
import org.adorsys.documentsafe.layer02service.KeyStoreService;
import org.adorsys.documentsafe.layer02service.exceptions.KeyStoreExistsException;
import org.adorsys.documentsafe.layer02service.generators.KeyStoreCreationConfig;
import org.adorsys.documentsafe.layer02service.generators.KeyStoreGenerator;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.KeyStoreAuth;
import org.adorsys.encobject.service.BlobStoreKeystorePersistence;
import org.adorsys.encobject.service.ExtendedStoreConnection;
import org.adorsys.encobject.service.KeystorePersistence;
import org.adorsys.encobject.types.KeyStoreType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;

public class KeyStoreServiceImpl implements KeyStoreService {
    private final static Logger LOGGER = LoggerFactory.getLogger(KeyStoreServiceImpl.class);

    private KeystorePersistence keystorePersistence;
    private BucketService bucketService;

    public KeyStoreServiceImpl(ExtendedStoreConnection extendedStoreConnection) {
        this.keystorePersistence = new BlobStoreKeystorePersistence(extendedStoreConnection);
        this.bucketService = new BucketServiceImpl(extendedStoreConnection);
    }

    /**
     *
     */
    @Override
    public void createKeyStore(KeyStoreAuth keyStoreAuth,
                               KeyStoreType keyStoreType,
                               BucketPath keyStorePath,
                               KeyStoreCreationConfig config) {
        try {
            LOGGER.info("start create keystore " + keyStorePath);
            if (bucketService.fileExists(keyStorePath)) {
                throw new KeyStoreExistsException("creation of keytore aborted. a keystore already exists in " + keyStorePath);
            }


            if (config == null) {
                config = new KeyStoreCreationConfig(5, 5, 5);
            }
            // TODO, hier also statt der StoreID nun das
            String serverKeyPairAliasPrefix = HexUtil.convertBytesToHexString(keyStorePath.getObjectHandle().getName().getBytes());
            LOGGER.info("keystoreid = " + serverKeyPairAliasPrefix);
            {
                String realKeyStoreId = new String(HexUtil.convertHexStringToBytes(serverKeyPairAliasPrefix));
                LOGGER.info("meaning of keystoreid = " + realKeyStoreId);
            }
            KeyStoreGenerator keyStoreGenerator = new KeyStoreGenerator(
                    config,
                    keyStoreType,
                    serverKeyPairAliasPrefix,
                    keyStoreAuth.getReadKeyPassword());
            KeyStore userKeyStore = keyStoreGenerator.generate();

            keystorePersistence.saveKeyStore(userKeyStore, keyStoreAuth.getReadStoreHandler(), keyStorePath.getObjectHandle());
            LOGGER.info("finished create keystore " + keyStorePath);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public KeyStore loadKeystore(BucketPath keyStorePath, CallbackHandler userKeystoreHandler) {
        LOGGER.info("start load keystore " + keyStorePath);
        KeyStore keyStore = keystorePersistence.loadKeystore(keyStorePath.getObjectHandle(), userKeystoreHandler);
        LOGGER.info("finished load keystore " + keyStorePath);
        return keyStore;
    }
}
