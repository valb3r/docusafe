package org.adorsys.docusafe.spring.config;

import org.adorsys.docusafe.cached.transactional.CachedTransactionalDocumentSafeService;
import org.adorsys.docusafe.spring.annotation.UseCachedTransactionalDocumentSafeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by peter on 02.10.18.
 */
@Configuration
// @UseSpringCachedTransactionalDocusafeServiceFactory
public class UseCachedTransactionalDocumentSafeServiceConfiguration {
    private final static Logger LOGGER = LoggerFactory.getLogger(UseCachedTransactionalDocumentSafeService.class);

    public UseCachedTransactionalDocumentSafeServiceConfiguration() {
        LOGGER.info("INIT");
    }

    @Bean
    public CachedTransactionalDocumentSafeService docusafeCachedTransactionalService(
//            SpringCachedTransactionalDocusafeServiceFactory springCachedTransactionalDocusafeServiceFactory,
            @Value("${docusafe.cache:true}") Boolean withCache
    ) {
        LOGGER.info(CachedTransactionalDocumentSafeService.class.getName() + " is required as @Bean");
//        return springCachedTransactionalDocusafeServiceFactory.getCachedTransactionalDocumentSafeServiceWithSubdir(null);
        return null;
    }
}
