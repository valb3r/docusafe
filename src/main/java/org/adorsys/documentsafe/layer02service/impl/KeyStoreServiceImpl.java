package org.adorsys.documentsafe.layer02service.impl;

import org.adorsys.documentsafe.layer00common.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer01persistence.ExtendedBlobStoreConnection;
import org.adorsys.documentsafe.layer01persistence.types.ListRecursiveFlag;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.KeyStoreBucketPath;
import org.adorsys.documentsafe.layer02service.BucketService;
import org.adorsys.documentsafe.layer02service.KeyStoreService;
import org.adorsys.documentsafe.layer02service.exceptions.KeyStoreExistsException;
import org.adorsys.documentsafe.layer02service.generators.KeyStoreGenerator;
import org.adorsys.documentsafe.layer02service.generators.SecretKeyGenerator;
import org.adorsys.documentsafe.layer01persistence.types.KeyStoreType;
import org.adorsys.documentsafe.layer02service.generators.KeyStoreCreationConfig;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.KeyStoreLocation;
import org.adorsys.documentsafe.layer01persistence.ExtendedKeystorePersistence;
import org.adorsys.documentsafe.layer01persistence.types.KeyStoreID;
import org.adorsys.documentsafe.layer02service.types.complextypes.BucketContent;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAuth;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;

public class KeyStoreServiceImpl implements KeyStoreService {
    private final static Logger LOGGER = LoggerFactory.getLogger(KeyStoreServiceImpl.class);

    private ExtendedKeystorePersistence keystorePersistence;
    private BucketService bucketService;

    public KeyStoreServiceImpl(BlobStoreContextFactory factory) {
        this.keystorePersistence = new ExtendedKeystorePersistence(factory);
        this.bucketService = new BucketServiceImpl(factory);
    }

    /**
     *
     * @param keyStoreID
     * @param keyStoreAuth
     * @param keystoreBucketPath
     * @param config may be null
     * @return
     */
    @Override
    public KeyStoreLocation createKeyStore(KeyStoreID keyStoreID,
                                           KeyStoreAuth keyStoreAuth,
                                           KeyStoreBucketPath keystoreBucketPath,
                                           KeyStoreCreationConfig config) {
        try {
            LOGGER.debug("start create keystore " + keyStoreID);
            {
                BucketContent bucketContent = bucketService.readDocumentBucket(keystoreBucketPath, ListRecursiveFlag.FALSE);
                for (StorageMetadata meta : bucketContent.getStrippedContent()) {
                    if (meta.getName().startsWith(keyStoreID.getValue())) {
                        throw new KeyStoreExistsException("creation of keytore aborted. a keystore with potentially the same type already exists in " + keystoreBucketPath.getObjectHandlePath() + " with name " + meta.getName());
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

            KeyStoreLocation keyStoreLocation = new KeyStoreLocation(keystoreBucketPath, keyStoreID, new KeyStoreType(userKeyStore.getType()));
			keystorePersistence.saveKeyStore(userKeyStore, keyStoreAuth.getReadStoreHandler(), keyStoreLocation);
            LOGGER.debug("finished create keystore " + keyStoreID + " @ " + keyStoreLocation);
			return keyStoreLocation;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
    
    @Override
    public KeyStore loadKeystore(KeyStoreLocation keyStoreLocation, CallbackHandler userKeystoreHandler){
        LOGGER.debug("start load keystore @ " + keyStoreLocation);
        KeyStore keyStore = keystorePersistence.loadKeystore(keyStoreLocation, userKeystoreHandler);
        LOGGER.debug("finished load keystore @ " + keyStoreLocation);
        return keyStore;
    }
}
