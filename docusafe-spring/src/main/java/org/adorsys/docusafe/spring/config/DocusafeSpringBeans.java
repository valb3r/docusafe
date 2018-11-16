package org.adorsys.docusafe.spring.config;

import org.adorsys.docusafe.spring.factory.SpringCachedTransactionalDocusafeServiceFactory;
import org.adorsys.docusafe.spring.factory.SpringExtendedStoreConnectionFactory;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by peter on 02.10.18.
 */
@Configuration
public class DocusafeSpringBeans {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocusafeSpringBeans.class);

    public DocusafeSpringBeans() {
        LOGGER.info("INIT");
    }

    @Bean
    public ExtendedStoreConnection extendedStoreConnection(SpringExtendedStoreConnectionFactory factory) {
        LOGGER.info(ExtendedStoreConnection.class.getName() + " is required as @Bean");
        return factory.getExtendedStoreConnectionWithSubDir(null);
    }

    @Bean
    public SpringExtendedStoreConnectionFactory factory(SpringDocusafeStoreconnectionProperties properties) {
        LOGGER.info(SpringExtendedStoreConnectionFactory.class.getName() + " is required as @Bean");
        return new SpringExtendedStoreConnectionFactory(properties);
    }

    /*
    @Bean
    SpringCachedTransactionalDocusafeServiceFactory factory(SpringExtendedStoreConnectionFactory connectionFactory,
                                                            @Value("${docusafe.cache:true}") Boolean withCache) {
        LOGGER.info(SpringCachedTransactionalDocusafeServiceFactory.class.getName() + " is required as @Bean");
        return new SpringCachedTransactionalDocusafeServiceFactory(connectionFactory, withCache);
    }

    @Bean
    public CachedTransactionalDocumentSafeService docusafeCachedTransactionalService(
            SpringCachedTransactionalDocusafeServiceFactory springCachedTransactionalDocusafeServiceFactory,
            @Value("${docusafe.cache:true}") Boolean withCache
    ) {
        LOGGER.info(CachedTransactionalDocumentSafeService.class.getName() + " is required as @Bean");
        return springCachedTransactionalDocusafeServiceFactory.getCachedTransactionalDocumentSafeServiceWithSubdir(null);
        return null;
    }
*/
}
