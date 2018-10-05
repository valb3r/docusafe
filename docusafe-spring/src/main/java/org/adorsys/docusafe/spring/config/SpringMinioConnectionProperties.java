package org.adorsys.docusafe.spring.config;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.miniostoreconnection.MinioExtendedStoreConnection;
import org.adorsys.encobject.types.connection.MinioAccessKey;
import org.adorsys.encobject.types.connection.MinioRootBucketName;
import org.adorsys.encobject.types.connection.MinioSecretKey;
import org.adorsys.encobject.types.properties.MinioConnectionProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.net.URL;

/**
 * Created by peter on 05.10.18.
 */
@Component
@ConfigurationProperties(prefix = "docusafe.storeconnection.minio")
@Validated
public class SpringMinioConnectionProperties extends SpringConnectionPropertiesImpl implements MinioConnectionProperties {
    public final static String template = "\n" +
            "docusafe:\n" +
            "  storeconnection:\n" +
            "    minio:\n" +
            "      url: (mandatory)\n" +
            "      accesskey: (mandatory)\n" +
            "      secretkey: (mandatory)\n" +
            "      rootbucket: (optional)\n" +
            SpringConnectionPropertiesImpl.template;

    private String url;
    private String rootbucket = defaultBucketname.getValue();
    private String accesskey;
    private String secretkey;

    @Override
    public URL getUrl() {
        try {
            return new URL(url);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }

    @Override
    public MinioRootBucketName getMinioRootBucketName() {
        return new MinioRootBucketName(rootbucket);
    }

    @Override
    public MinioAccessKey getMinioAccessKey() {
        return new MinioAccessKey(accesskey);
    }

    @Override
    public MinioSecretKey getMinioSecretKey() {
        return new MinioSecretKey(secretkey);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setRootbucket(String rootbucket) {
        this.rootbucket = rootbucket;
    }

    public void setAccesskey(String accesskey) {
        this.accesskey = accesskey;
    }

    public void setSecretkey(String secretkey) {
        this.secretkey = secretkey;
    }
}
