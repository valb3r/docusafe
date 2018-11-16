package org.adorsys.docusafe.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


/**
 * Created by peter on 02.10.18.
 */

@Configuration
@ComponentScan(basePackages = {
        "org.adorsys.docusafe.spring.config"
})
public class DocusafeSpringConfiguration {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocusafeSpringConfiguration.class);
    public DocusafeSpringConfiguration() {
        LOGGER.debug("docusafe spring config supplies ExtendedStoreConnection and CachedTransactionalDocumentSafeService as spring beans.");
    }
}
