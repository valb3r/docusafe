package org.adorsys.docusafe.spring.config;

import org.adorsys.docusafe.spring.factory.SpringExtendedStoreConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by peter on 14.11.18 14:19.
 */
@Configuration
public class UseSpringExtendedStoreConnectionFactoryConfiguration {
    private final static Logger LOGGER = LoggerFactory.getLogger(UseSpringExtendedStoreConnectionFactoryConfiguration.class);

    public UseSpringExtendedStoreConnectionFactoryConfiguration() {
        LOGGER.info("INIT");
    }

    @Bean
    public SpringExtendedStoreConnectionFactory factory(SpringDocusafeStoreconnectionProperties properties) {
        LOGGER.info(SpringExtendedStoreConnectionFactory.class.getName() + " is required as @Bean");
        return new SpringExtendedStoreConnectionFactory(properties);
    }
}
