package org.adorsys.docusafe.spring.config;

import org.adorsys.cryptoutils.exceptions.BaseException;
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
@ComponentScan(basePackages = {
        "org.adorsys.docusafe.spring.config"
})
public class UseSpringExtendedStoreConnectionFactoryConfiguration {
    private final static Logger LOGGER = LoggerFactory.getLogger(UseSpringExtendedStoreConnectionFactoryConfiguration.class);

    @Bean
    public SpringExtendedStoreConnectionFactory factory(SpringDocusafeStoreconnectionProperties properties) {
        LOGGER.info(SpringExtendedStoreConnectionFactory.class.getName() + " is required as @Bean");
        if (properties == null) {
            throw new BaseException("Injection of " + SpringDocusafeStoreconnectionProperties.class.getName() + " did not work");
        }
        return new SpringExtendedStoreConnectionFactory(properties);
    }
}
