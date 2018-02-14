package org.adorsys.documentsafe.layer02service;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer02service.generators.KeyStoreCreationConfig;
import org.adorsys.documentsafe.layer02service.impl.KeyStoreServiceImpl;
import org.adorsys.documentsafe.layer02service.types.ReadKeyPassword;
import org.adorsys.documentsafe.layer02service.types.ReadStorePassword;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAuth;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.service.ExtendedStoreConnection;
import org.adorsys.encobject.types.KeyStoreType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyStore;

/**
 * Created by peter on 02.01.18.
 */
public class KeyStoreServiceTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(KeyStoreServiceTest.class);

    private static String keystoreContainer = "keystore-container-" + KeyStoreServiceTest.class.getSimpleName();
    private ExtendedStoreConnection extendedStoreConnection;


    public KeyStoreServiceTest(ExtendedStoreConnection extendedStoreConnection) {
        this.extendedStoreConnection = extendedStoreConnection;
    }

    public KeyStoreStuff createKeyStore() {
        return createKeyStore(keystoreContainer, new ReadStorePassword("storePassword"), new ReadKeyPassword("keypassword"), "key-store-id-123", null);
    }

    public KeyStoreStuff createKeyStore(String keystoreContainer,
                                        ReadStorePassword readStorePassword,
                                        ReadKeyPassword readKeyPassword,
                                        String keyStoreID,
                                        KeyStoreCreationConfig config) {
        try {
            BucketDirectory keyStoreDirectory = new BucketDirectory(keystoreContainer);

            ContainerPersistence containerPersistence = new ContainerPersistence(extendedStoreConnection);
            try {
                // sollte der container exsitieren, ignorieren wir die Exception, um zu
                // sehen, ob sich ein keystore überschreiben lässt
                containerPersistence.creteContainer(keyStoreDirectory.getObjectHandle().getContainer());
            } catch (Exception e) {
                LOGGER.error("Exception is ignored");
            }
            AllServiceTest.buckets.add(keyStoreDirectory);

            KeyStoreService keyStoreService = new KeyStoreServiceImpl(extendedStoreConnection);
            KeyStoreAuth keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);
            BucketPath keyStorePath = keyStoreDirectory.appendName(keyStoreID);
            keyStoreService.createKeyStore(keyStoreAuth, new KeyStoreType("UBER"), keyStorePath, config);
            KeyStore keyStore = keyStoreService.loadKeystore(keyStorePath, keyStoreAuth.getReadStoreHandler());
            return new KeyStoreStuff(keyStore, extendedStoreConnection, new KeyStoreAccess(keyStorePath, keyStoreAuth));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }


    public static class KeyStoreStuff {
        public final KeyStore keyStore;
        public final ExtendedStoreConnection extendedStoreConnection;
        public final KeyStoreAccess keyStoreAccess;


        public KeyStoreStuff(KeyStore keyStore, ExtendedStoreConnection extendedStoreConnection, KeyStoreAccess keyStoreAccess) {
            this.keyStore = keyStore;
            this.extendedStoreConnection = extendedStoreConnection;
            this.keyStoreAccess = keyStoreAccess;
        }
    }
}
