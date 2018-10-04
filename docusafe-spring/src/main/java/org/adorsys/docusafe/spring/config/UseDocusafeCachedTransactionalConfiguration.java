package org.adorsys.docusafe.spring.config;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.impl.DocumentSafeServiceImpl;
import org.adorsys.docusafe.business.impl.WithCache;
import org.adorsys.docusafe.cached.transactional.CachedTransactionalDocumentSafeService;
import org.adorsys.docusafe.cached.transactional.impl.CachedTransactionalDocumentSafeServiceImpl;
import org.adorsys.docusafe.spring.SimpleRequestMemoryContextImpl;
import org.adorsys.docusafe.spring.annotation.UseDocusafeCachedTransactional;
import org.adorsys.docusafe.spring.annotation.UseExtendedStoreConnection;
import org.adorsys.docusafe.transactional.RequestMemoryContext;
import org.adorsys.docusafe.transactional.TransactionalDocumentSafeService;
import org.adorsys.docusafe.transactional.impl.TransactionalDocumentSafeServiceImpl;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by peter on 02.10.18.
 */
@UseExtendedStoreConnection
@Configuration
public class UseDocusafeCachedTransactionalConfiguration {
    private final static Logger LOGGER = LoggerFactory.getLogger(UseDocusafeCachedTransactional.class);

    @Bean
    public CachedTransactionalDocumentSafeService docusafeCachedTransactionalService(
            ExtendedStoreConnection extendedStoreConnection,
            @Value("${docusafe.cache:true}") Boolean withCache
    ) {
        if (extendedStoreConnection == null) {
            throw new BaseException("Injection did not work");
        }
        LOGGER.debug("create documentSafeService");
        DocumentSafeService documentSafeService = new DocumentSafeServiceImpl(withCache ? WithCache.TRUE : WithCache.FALSE, extendedStoreConnection);
        RequestMemoryContext requestContext = new SimpleRequestMemoryContextImpl();
        LOGGER.debug("create transactionalDocumentSafeService");
        TransactionalDocumentSafeService transactionalDocumentSafeService = new TransactionalDocumentSafeServiceImpl(requestContext, documentSafeService);
        LOGGER.debug("create cachedTransactionalDocumentSafeService");
        CachedTransactionalDocumentSafeService cachedTransactionalDocumentSafeService = new CachedTransactionalDocumentSafeServiceImpl(requestContext, transactionalDocumentSafeService);
        return cachedTransactionalDocumentSafeService;
    }
}
