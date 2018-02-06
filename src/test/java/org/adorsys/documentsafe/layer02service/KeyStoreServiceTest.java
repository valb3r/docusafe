package org.adorsys.documentsafe.layer02service;

import java.security.KeyStore;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer02service.generators.KeyStoreCreationConfig;
import org.adorsys.documentsafe.layer02service.impl.KeyStoreServiceImpl;
import org.adorsys.documentsafe.layer02service.types.ReadKeyPassword;
import org.adorsys.documentsafe.layer02service.types.ReadStorePassword;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAuth;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.complextypes.KeyStoreDirectory;
import org.adorsys.encobject.complextypes.KeyStoreLocation;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.service.ExtendedStoreConnection;
import org.adorsys.encobject.types.KeyStoreID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        return createKeyStore(keystoreContainer, new ReadStorePassword("storePassword"), new ReadKeyPassword("keypassword"), new KeyStoreID("key-store-id-123"), null);
    }

    public KeyStoreStuff createKeyStore(String keystoreContainer,
                                        ReadStorePassword readStorePassword,
                                        ReadKeyPassword readKeyPassword,
                                        KeyStoreID keyStoreID,
                                        KeyStoreCreationConfig config) {
        try {
            KeyStoreDirectory keyStoreDirectory = new KeyStoreDirectory(new BucketPath(keystoreContainer));

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
            KeyStoreLocation keyStoreLocation = keyStoreService.createKeyStore(keyStoreID, keyStoreAuth, keyStoreDirectory, config);
            KeyStore keyStore = keyStoreService.loadKeystore(keyStoreLocation, keyStoreAuth.getReadStoreHandler());
            return new KeyStoreStuff(keyStore, extendedStoreConnection, keyStoreID, new KeyStoreAccess(keyStoreLocation, keyStoreAuth));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }


    public static class KeyStoreStuff {
        public final KeyStore keyStore;
        public final ExtendedStoreConnection extendedStoreConnection;
        public final KeyStoreAccess keyStoreAccess;


        public KeyStoreStuff(KeyStore keyStore, ExtendedStoreConnection extendedStoreConnection, KeyStoreID keyStoreID, KeyStoreAccess keyStoreAccess) {
            this.keyStore = keyStore;
            this.extendedStoreConnection = extendedStoreConnection;
            this.keyStoreAccess = keyStoreAccess;
        }
    }
}
