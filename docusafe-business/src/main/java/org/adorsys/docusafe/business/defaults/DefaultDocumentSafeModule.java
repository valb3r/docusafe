package org.adorsys.docusafe.business.defaults;

import dagger.Module;
import dagger.Provides;
import org.adorsys.docusafe.business.impl.CacheType;
import org.adorsys.docusafe.business.impl.DocumentSafeServiceImpl;
import org.adorsys.docusafe.business.impl.DocusafeCacheWrapper;
import org.adorsys.docusafe.business.impl.DocusafeCacheWrapperImpl;
import org.adorsys.docusafe.service.BucketService;
import org.adorsys.docusafe.service.DocumentGuardService;
import org.adorsys.docusafe.service.DocumentPersistenceService;
import org.adorsys.docusafe.service.KeySourceService;
import org.adorsys.docusafe.service.impl.*;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.api.KeyStoreService;
import org.adorsys.encobject.service.impl.KeyStoreServiceImpl;

@Module
public class DefaultDocumentSafeModule {

    @Provides
    BucketService bucketService(ExtendedStoreConnection conn) {
        return new BucketServiceImpl(conn);
    }

    @Provides
    KeyStoreService keyStoreService(ExtendedStoreConnection conn) {
        return new KeyStoreServiceImpl(conn);
    }

    @Provides
    DocumentGuardService documentGuardService(ExtendedStoreConnection conn) {
        return new DocumentGuardServiceImpl(conn);
    }

    @Provides
    DocumentPersistenceService documentPersistenceService(ExtendedStoreConnection conn, DocumentSafeServiceImpl cache) {
        return new DocumentPersistenceServiceImpl(conn, cache);
    }

    @Provides
    KeySourceService keySourceService(ExtendedStoreConnection conn) {
        return new KeySourceServiceImpl(conn);
    }

    @Provides
    DocusafeCacheWrapper docusafeCacheWrapper(ExtendedStoreConnection conn) {
        return new DocusafeCacheWrapperImpl(CacheType.GUAVA);
    }
}
