package org.adorsys.docusafe.business;

import dagger.BindsInstance;
import dagger.Component;
import org.adorsys.docusafe.business.defaults.DefaultDocumentSafeModule;
import org.adorsys.docusafe.business.impl.DocumentSafeServiceImpl;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;

import javax.inject.Singleton;

@Singleton
@Component(modules = DefaultDocumentSafeModule.class)
public interface DefaultDocumentSafeService {

    DocumentSafeServiceImpl documentSafeService();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder storeConnection(ExtendedStoreConnection storeConnection);

        DefaultDocumentSafeService build();
    }
}
