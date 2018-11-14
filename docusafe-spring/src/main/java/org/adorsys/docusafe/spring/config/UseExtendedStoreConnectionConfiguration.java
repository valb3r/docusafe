package org.adorsys.docusafe.spring.config;

import org.adorsys.docusafe.spring.annotation.UseSimpleExtendedStoreConnectionFactory;
import org.adorsys.docusafe.spring.factory.SimpleSubdirFactory;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by peter on 02.10.18.
 */
@UseSimpleExtendedStoreConnectionFactory
@Configuration
public class UseExtendedStoreConnectionConfiguration {
    private final static Logger LOGGER = LoggerFactory.getLogger(UseExtendedStoreConnectionConfiguration.class);

    @Bean
    public ExtendedStoreConnection extendedStoreConnection(SimpleSubdirFactory factory) {
        LOGGER.info(ExtendedStoreConnection.class.getName() + " is required as @Bean");
        return factory.getExtendedStoreConnectionWithSubDir(null);
    }


}
