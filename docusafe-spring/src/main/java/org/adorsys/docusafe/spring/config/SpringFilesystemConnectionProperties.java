package org.adorsys.docusafe.spring.config;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.encobject.types.connection.FilesystemBasedirectoryName;
import org.adorsys.encobject.types.properties.FilesystemConnectionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Created by peter on 05.10.18.
 */
@Component
@ConfigurationProperties(prefix = "docusafe.storeconnection.filesystem")
@Validated
public class SpringFilesystemConnectionProperties extends SpringConnectionPropertiesImpl implements FilesystemConnectionProperties {
    private final static Logger LOGGER = LoggerFactory.getLogger(SpringFilesystemConnectionProperties.class);

    @Nullable
    private String basedir = defaultBasedirectory.getValue();

    @Override
    public FilesystemBasedirectoryName getFilesystemBasedirectoryName() {
        if (basedir == null) {
            throw new BaseException("basedir must not be null");
        }
        LOGGER.debug("basedir:" + new FilesystemBasedirectoryName(basedir));
        return new FilesystemBasedirectoryName(basedir);
    }

    public void setBasedir(String basedir) {
        this.basedir = basedir;
    }





}
