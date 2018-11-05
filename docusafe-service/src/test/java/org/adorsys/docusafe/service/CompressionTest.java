package org.adorsys.docusafe.service;

import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.docusafe.service.utils.TestKeyUtils;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.KeyStoreAccess;
import org.adorsys.encobject.domain.KeyStoreAuth;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.adorsys.encobject.domain.ReadStorePassword;
import org.adorsys.encobject.service.api.ContainerPersistence;
import org.adorsys.encobject.service.api.EncryptedPersistenceService;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.api.KeySource;
import org.adorsys.encobject.service.api.KeyStore2KeySourceHelper;
import org.adorsys.encobject.service.api.KeyStoreService;
import org.adorsys.encobject.service.api.KeystorePersistence;
import org.adorsys.encobject.service.impl.AESEncryptionStreamServiceImpl;
import org.adorsys.encobject.service.impl.BlobStoreKeystorePersistenceImpl;
import org.adorsys.encobject.service.impl.ContainerPersistenceImpl;
import org.adorsys.encobject.service.impl.EncryptedPersistenceServiceImpl;
import org.adorsys.encobject.service.impl.JWEncryptionStreamServiceImpl;
import org.adorsys.encobject.service.impl.KeyStoreServiceImpl;
import org.adorsys.encobject.service.impl.SimplePayloadImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
import org.adorsys.jkeygen.keystore.KeyStoreType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by peter on 26.02.18 at 10:04.
 */
public class CompressionTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(CompressionTest.class);
    public static Set<BucketDirectory> buckets = new HashSet<>();
    private ExtendedStoreConnection extendedStoreConnection = ExtendedStoreConnectionFactory.get();

    @Before
    public void before() {
        TestKeyUtils.turnOffEncPolicy();
        buckets.clear();
    }

    @After
    public void after() {
        ContainerPersistence containerPersistence = new ContainerPersistenceImpl(extendedStoreConnection);
        for (BucketDirectory bucket : buckets) {
            try {
                LOGGER.debug("AFTER TEST: DELETE BUCKET " + bucket);
                containerPersistence.deleteContainer(bucket);
            } catch (Exception e) {
                // ignore Exception
            }
        }
    }


    @Test
    public void testCompressionWithPrivateAndPublicKey() {
        

        BucketDirectory bd = new BucketDirectory("containerforcompressiontestwithprivatepublickeypay");
        LOGGER.debug("instance of extendedStoreConnection is " + extendedStoreConnection.getClass().getName());
        extendedStoreConnection.createContainer(bd);
        buckets.add(bd);

        KeyStoreService keyStoreService = new KeyStoreServiceImpl(extendedStoreConnection);
        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(new ReadStorePassword("storepassword"), new ReadKeyPassword("keypassword"));

        BucketPath keystorePath = bd.appendName("keystore");
        keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, keystorePath, null);

        BucketPath documentPath1 = bd.appendName("document1");
        BucketPath documentPath2 = bd.appendName("document2");
        int size = 2000;
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            data[i] = (byte) i;
        }

        EncryptedPersistenceService encryptedPersistenceService = new EncryptedPersistenceServiceImpl(extendedStoreConnection, new JWEncryptionStreamServiceImpl());

        SimpleStorageMetadataImpl storageMetadata = new SimpleStorageMetadataImpl();
        storageMetadata.setShouldBeCompressed(Boolean.TRUE);
        Payload payloadWithCompress = new SimplePayloadImpl(storageMetadata, data);

        SimpleStorageMetadataImpl storageMetadata2 = new SimpleStorageMetadataImpl();
        storageMetadata2.setShouldBeCompressed(Boolean.FALSE);
        Payload payloadWithoutCompress = new SimplePayloadImpl(storageMetadata2, data);

        KeystorePersistence keystorePersistence = new BlobStoreKeystorePersistenceImpl(extendedStoreConnection);
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keystorePath, keyStoreAuth);

        KeyStore2KeySourceHelper.KeySourceAndKeyID publicKeySource = KeyStore2KeySourceHelper.getForPublicKey(keystorePersistence, keyStoreAccess);
        KeySource privateKeySource = KeyStore2KeySourceHelper.getForPrivateKey(keystorePersistence, keyStoreAccess);

        encryptedPersistenceService.encryptAndPersist(documentPath1, payloadWithCompress, publicKeySource.getKeySource(), publicKeySource.getKeyID());
        encryptedPersistenceService.encryptAndPersist(documentPath2, payloadWithoutCompress, publicKeySource.getKeySource(), publicKeySource.getKeyID());

        Payload loadedPayloadWithCompress = encryptedPersistenceService.loadAndDecrypt(documentPath1, privateKeySource);
        Payload loadedPayloadWithoutCompress = encryptedPersistenceService.loadAndDecrypt(documentPath2, privateKeySource);
        Assert.assertTrue(Arrays.equals(loadedPayloadWithCompress.getData(), data));
        Assert.assertTrue(Arrays.equals(loadedPayloadWithoutCompress.getData(), data));

        LOGGER.debug("size compressed:" + loadedPayloadWithCompress.getStorageMetadata().getSize());
        LOGGER.debug("size uncompressed:" + loadedPayloadWithoutCompress.getStorageMetadata().getSize());

        // TODO
        LOGGER.warn("COMPRESSION DOES NOT WORK YET");

/*
        int encryptedUncrompressedSize = Integer.parseInt(plainStorageMetadata2.getUserMetadata().get(extendedZipFileHelper.getCompressedKey()));
        int encryptedCrompressedSize = Integer.parseInt(plainStorageMetadata1.getUserMetadata().get(extendedZipFileHelper.getCompressedKey()));
        int diff = encryptedUncrompressedSize - encryptedCrompressedSize;
        LOGGER.debug("encrypted size compressed:" + encryptedCrompressedSize);
        LOGGER.debug("encrypted size uncompressed:" + encryptedUncrompressedSize);
        LOGGER.debug("compression saves bytes:" + diff);
*/

        // Assert.assertTrue(diff > 0);
    }


    @Test
    public void testCompressionWithSecretKey() {
        

        BucketDirectory bd = new BucketDirectory("containerforcompressiontestwithsecretkey");
        extendedStoreConnection.createContainer(bd);
        buckets.add(bd);

        KeyStoreService keyStoreService = new KeyStoreServiceImpl(extendedStoreConnection);
        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(new ReadStorePassword("storepassword"), new ReadKeyPassword("keypassword"));

        BucketPath keystorePath = bd.appendName("keystore");
        keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, keystorePath, null);

        BucketPath documentPath1 = bd.appendName("document1");
        BucketPath documentPath2 = bd.appendName("document2");
        int size = 2000;
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            data[i] = (byte) i;
        }

        EncryptedPersistenceService encryptedPersistenceService = new EncryptedPersistenceServiceImpl(extendedStoreConnection, new AESEncryptionStreamServiceImpl());

        SimpleStorageMetadataImpl storageMetadata = new SimpleStorageMetadataImpl();
        storageMetadata.setShouldBeCompressed(Boolean.TRUE);
        Payload payloadWithCompress = new SimplePayloadImpl(storageMetadata, data);

        SimpleStorageMetadataImpl storageMetadata2 = new SimpleStorageMetadataImpl();
        storageMetadata2.setShouldBeCompressed(Boolean.FALSE);
        Payload payloadWithoutCompress = new SimplePayloadImpl(storageMetadata2, data);

        KeystorePersistence keystorePersistence = new BlobStoreKeystorePersistenceImpl(extendedStoreConnection);
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keystorePath, keyStoreAuth);

        KeyStore2KeySourceHelper.KeySourceAndKeyID secretKeySourceAndID = KeyStore2KeySourceHelper.getForSecretKey(keystorePersistence, keyStoreAccess);

        encryptedPersistenceService.encryptAndPersist(documentPath1, payloadWithCompress, secretKeySourceAndID.getKeySource(), secretKeySourceAndID.getKeyID());
        encryptedPersistenceService.encryptAndPersist(documentPath2, payloadWithoutCompress, secretKeySourceAndID.getKeySource(), secretKeySourceAndID.getKeyID());

        Payload loadedPayloadWithCompress = encryptedPersistenceService.loadAndDecrypt(documentPath1, secretKeySourceAndID.getKeySource());
        Payload loadedPayloadWithoutCompress = encryptedPersistenceService.loadAndDecrypt(documentPath2, secretKeySourceAndID.getKeySource());
        Assert.assertTrue(Arrays.equals(loadedPayloadWithCompress.getData(), data));
        Assert.assertTrue(Arrays.equals(loadedPayloadWithoutCompress.getData(), data));

        LOGGER.debug("size compressed:" + loadedPayloadWithCompress.getStorageMetadata().getSize());
        LOGGER.debug("size uncompressed:" + loadedPayloadWithoutCompress.getStorageMetadata().getSize());

        // TODO
        LOGGER.warn("COMPRESSION DOES NOT WORK YET");


/*
        int encryptedUncrompressedSize = Integer.parseInt(plainStorageMetadata2.getUserMetadata().get(extendedZipFileHelper.getCompressedKey()));
        int encryptedCrompressedSize = Integer.parseInt(plainStorageMetadata1.getUserMetadata().get(extendedZipFileHelper.getCompressedKey()));
        int diff = encryptedUncrompressedSize - encryptedCrompressedSize;
        LOGGER.debug("encrypted size compressed:" + encryptedCrompressedSize);
        LOGGER.debug("encrypted size uncompressed:" + encryptedUncrompressedSize);
        LOGGER.debug("compression saves bytes:" + diff);
*/
        // Assert.assertTrue(diff > 0);
    }


}
