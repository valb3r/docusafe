package org.adorsys.documentsafe.layer02service.impl;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer02service.BucketService;
import org.adorsys.documentsafe.layer02service.KeyStoreService;
import org.adorsys.documentsafe.layer02service.exceptions.KeyStoreExistsException;
import org.adorsys.documentsafe.layer02service.generators.KeyStoreCreationConfig;
import org.adorsys.documentsafe.layer02service.generators.KeyStoreGenerator;
import org.adorsys.documentsafe.layer02service.types.complextypes.BucketContent;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAuth;
import org.adorsys.encobject.complextypes.KeyStoreDirectory;
import org.adorsys.encobject.complextypes.KeyStoreLocation;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.service.BlobStoreKeystorePersistence;
import org.adorsys.encobject.service.ExtendedStoreConnection;
import org.adorsys.encobject.service.KeystorePersistence;
import org.adorsys.encobject.types.KeyStoreID;
import org.adorsys.encobject.types.KeyStoreType;
import org.adorsys.encobject.types.ListRecursiveFlag;
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
    public KeyStoreLocation createKeyStore(KeyStoreID keyStoreID,
                                           KeyStoreAuth keyStoreAuth,
                                           KeyStoreDirectory keyStoreDirectory,
                                           KeyStoreCreationConfig config) {
        try {
            LOGGER.info("start create keystore " + keyStoreID);
            {
                BucketContent bucketContent = bucketService.readDocumentBucket(keyStoreDirectory, ListRecursiveFlag.FALSE);
                for (StorageMetadata meta : bucketContent.getStrippedContent()) {
                    if (meta.getName().startsWith(keyStoreID.getValue())) {
                        throw new KeyStoreExistsException("creation of keytore aborted. a keystore with potentially the same type already exists in " + keyStoreDirectory + " with name " + meta.getName());
                    }
                }
            }


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

            KeyStoreLocation keyStoreLocation = new KeyStoreLocation(keyStoreDirectory, keyStoreID, new KeyStoreType(userKeyStore.getType()));
            LOGGER.debug("keystore location is " + keyStoreLocation);
			keystorePersistence.saveKeyStore(userKeyStore, keyStoreAuth.getReadStoreHandler(), keyStoreLocation.getLocationHandle());
            LOGGER.info("finished create keystore " + keyStoreID + " at " + keyStoreLocation);
			return keyStoreLocation;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
    
    @Override
    public KeyStore loadKeystore(KeyStoreLocation keyStoreLocation, CallbackHandler userKeystoreHandler){
        LOGGER.info("start load keystore @ " + keyStoreLocation);
        KeyStore keyStore = keystorePersistence.loadKeystore(keyStoreLocation.getLocationHandle(), userKeystoreHandler);
        LOGGER.info("finished load keystore @ " + keyStoreLocation);
        return keyStore;
    }
}
