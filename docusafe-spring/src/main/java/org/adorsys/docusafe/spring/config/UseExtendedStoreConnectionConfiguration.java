package org.adorsys.docusafe.spring.config;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.types.properties.FilesystemConnectionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.StringTokenizer;

/**
 * Created by peter on 02.10.18.
 */
@Configuration
@ComponentScan(basePackages = {
        "org.adorsys.docusafe.spring.config"
})
public class UseExtendedStoreConnectionConfiguration {
    private final static Logger LOGGER = LoggerFactory.getLogger(UseExtendedStoreConnectionConfiguration.class);

    @Bean
    public ExtendedStoreConnection extendedStoreConnection(SpringDocusafeStoreconnectionProperties properties) {
        if (properties.getFilesystem() != null) {
            LOGGER.debug("jetzt filesystem");
            return ExtendedStoreConnectionFactory.get(properties.getFilesystem());
        }
        if (properties.getAmazons3() != null) {
            LOGGER.debug("jetzt amazon");
            return ExtendedStoreConnectionFactory.get(properties.getAmazons3());
        }
        throw new BaseException("at least filesystem or amazons3 has to be specified");
    }
}
