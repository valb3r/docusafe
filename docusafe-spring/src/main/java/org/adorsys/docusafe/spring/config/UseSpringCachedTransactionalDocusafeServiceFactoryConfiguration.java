package org.adorsys.docusafe.spring.config;

import org.adorsys.docusafe.spring.annotation.UseSpringExtendedStoreConnectionFactory;
import org.adorsys.docusafe.spring.factory.SpringCachedTransactionalDocusafeServiceFactory;
import org.adorsys.docusafe.spring.factory.SpringExtendedStoreConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by peter on 14.11.18 20:25.
 */

public class UseSpringCachedTransactionalDocusafeServiceFactoryConfiguration {
}

/*
@Configuration
@UseSpringExtendedStoreConnectionFactory
public class UseSpringCachedTransactionalDocusafeServiceFactoryConfiguration {
    private final static Logger LOGGER = LoggerFactory.getLogger(UseSpringCachedTransactionalDocusafeServiceFactoryConfiguration.class);

    public UseSpringCachedTransactionalDocusafeServiceFactoryConfiguration() {
        LOGGER.info("INIT");
    }

    @Bean
    SpringCachedTransactionalDocusafeServiceFactory factory(SpringExtendedStoreConnectionFactory connectionFactory,
                                                            @Value("${docusafe.cache:true}") Boolean withCache) {
        LOGGER.info(SpringCachedTransactionalDocusafeServiceFactory.class.getName() + " is required as @Bean");
        return new SpringCachedTransactionalDocusafeServiceFactory(connectionFactory, withCache);
    }
}
*/