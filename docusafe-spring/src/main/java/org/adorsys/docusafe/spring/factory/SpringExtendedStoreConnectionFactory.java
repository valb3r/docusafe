package org.adorsys.docusafe.spring.factory;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.extendendstoreconnection.impl.amazons3.AmazonS3ConnectionProperitesImpl;
import org.adorsys.cryptoutils.miniostoreconnection.MinioConnectionPropertiesImpl;
import org.adorsys.cryptoutils.mongodbstoreconnection.MongoConnectionPropertiesImpl;
import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.docusafe.spring.config.SpringAmazonS3ConnectionProperties;
import org.adorsys.docusafe.spring.config.SpringDocusafeStoreconnectionProperties;
import org.adorsys.docusafe.spring.config.SpringFilesystemConnectionProperties;
import org.adorsys.docusafe.spring.config.SpringMinioConnectionProperties;
import org.adorsys.docusafe.spring.config.SpringMongoConnectionProperties;
import org.adorsys.encobject.filesystem.FilesystemConnectionPropertiesImpl;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.types.connection.AmazonS3RootBucketName;
import org.adorsys.encobject.types.connection.FilesystemRootBucketName;
import org.adorsys.encobject.types.connection.MinioRootBucketName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 14.11.18 12:05.
 */
public class SpringExtendedStoreConnectionFactory {
    private final static Logger LOGGER = LoggerFactory.getLogger(SpringExtendedStoreConnectionFactory.class);
    private SpringDocusafeStoreconnectionProperties wiredProperties;

    public SpringExtendedStoreConnectionFactory(SpringDocusafeStoreconnectionProperties wiredProperties) {
        this.wiredProperties = wiredProperties;
    }

    public ExtendedStoreConnection getExtendedStoreConnectionWithSubDir(String basedir) {
        if (wiredProperties.getFilesystem() != null) {
            FilesystemConnectionPropertiesImpl properties = new FilesystemConnectionPropertiesImpl(wiredProperties.getFilesystem());
            if (basedir !=  null) {
                String origName = properties.getFilesystemRootBucketName().getValue();
                String newName = origName + basedir;
                properties.setFilesystemRootBucketName(new FilesystemRootBucketName(newName));
            }
            LOGGER.debug("jetzt filesystem");
            return ExtendedStoreConnectionFactory.get(properties);
        }
        if (wiredProperties.getAmazons3() != null) {
            AmazonS3ConnectionProperitesImpl properties = new AmazonS3ConnectionProperitesImpl(wiredProperties.getAmazons3());
            if (basedir !=  null) {
                String origName = properties.getAmazonS3RootBucketName().getValue();
                String newName = origName + basedir;
                properties.setAmazonS3RootBucketName(new AmazonS3RootBucketName(newName));
            }
            LOGGER.debug("jetzt amazon");
            return ExtendedStoreConnectionFactory.get(properties);
        }
        if (wiredProperties.getMinio() != null) {
            MinioConnectionPropertiesImpl properties = new MinioConnectionPropertiesImpl(wiredProperties.getMinio());
            if (basedir !=  null) {
                String origName = properties.getMinioRootBucketName().getValue();
                String newName = origName + basedir;
                properties.setMinioRootBucketName(new MinioRootBucketName(newName));
            }
            LOGGER.debug("jetzt minio");
            return ExtendedStoreConnectionFactory.get(properties);
        }
        if (wiredProperties.getMongo() != null) {
            MongoConnectionPropertiesImpl properties = new MongoConnectionPropertiesImpl(wiredProperties.getMongo());
            if (basedir !=  null) {
                properties.setMongoURI(new MongoURIChanger(properties.getMongoURI()).modifyRootBucket(basedir));
            }
            LOGGER.debug("jetzt mongo");
            return ExtendedStoreConnectionFactory.get(properties);
        }
        String emessage = "at least filesystem, amazons3, minio or mongo has to be specified with ";
        String message = emessage +
                SpringFilesystemConnectionProperties.template +
                SpringAmazonS3ConnectionProperties.template +
                SpringMinioConnectionProperties.template +
                SpringMongoConnectionProperties.template;
        LOGGER.error(message);
        throw new BaseException(emessage);
    }



}
