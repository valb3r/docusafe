package org.adorsys.docusafe.spring.factory;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.impl.DocumentSafeServiceImpl;
import org.adorsys.docusafe.business.impl.WithCache;
import org.adorsys.docusafe.cached.transactional.CachedTransactionalDocumentSafeService;
import org.adorsys.docusafe.cached.transactional.impl.CachedTransactionalDocumentSafeServiceImpl;
import org.adorsys.docusafe.spring.SimpleRequestMemoryContextImpl;
import org.adorsys.docusafe.transactional.RequestMemoryContext;
import org.adorsys.docusafe.transactional.TransactionalDocumentSafeService;
import org.adorsys.docusafe.transactional.impl.TransactionalDocumentSafeServiceImpl;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 14.11.18 20:24.
 */
public class SpringCachedTransactionalDocusafeServiceFactory {
    private final static Logger LOGGER = LoggerFactory.getLogger(SpringCachedTransactionalDocusafeServiceFactory.class);
    private SpringExtendedStoreConnectionFactory connectionFactory;
    private Boolean withCache;

    public SpringCachedTransactionalDocusafeServiceFactory(SpringExtendedStoreConnectionFactory connectionFactory, Boolean withCache) {
        this.connectionFactory = connectionFactory;
        this.withCache = withCache;
    }

    public CachedTransactionalDocumentSafeService getCachedTransactionalDocumentSafeServiceWithSubdir(String basedir) {
        if (connectionFactory == null) {
            throw new BaseException("Injection did not work for " + SpringExtendedStoreConnectionFactory.class.getName());
        }
        LOGGER.info("getExtendedStoreConnection");
        ExtendedStoreConnection extendedStoreConnection = connectionFactory.getExtendedStoreConnectionWithSubDir(basedir);
        LOGGER.info(CachedTransactionalDocumentSafeService.class.getName() + " is required as @Bean");
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
