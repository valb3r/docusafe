package org.adorsys.docusafe.spring.config;

import org.adorsys.encobject.types.BucketPathEncryptionPassword;
import org.adorsys.encobject.types.properties.ConnectionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * Created by peter on 05.10.18.
 */
public class SpringConnectionPropertiesImpl implements ConnectionProperties {
    private final static Logger LOGGER = LoggerFactory.getLogger(SpringConnectionPropertiesImpl.class);

    @Nullable
    private String encryptionpassword = defaultEncryptionPassword.getValue();

    @Override
    public BucketPathEncryptionPassword getBucketPathEncryptionPassword() {
        if (encryptionpassword == null || encryptionpassword.length() == 0) {
            LOGGER.debug("encryptionpassword is:null");
            return null;
        }
        LOGGER.debug("encryptionpassword is:\"" + encryptionpassword + "\"");
        LOGGER.debug("encryptionpassword is:\"" + new BucketPathEncryptionPassword(encryptionpassword) + "\"");
        return new BucketPathEncryptionPassword(encryptionpassword);
    }

    public void setEncryptionpassword(String encryptionpassword) {
        this.encryptionpassword = encryptionpassword;
    }


}
