package org.adorsys.docusafe.business;

import dagger.BindsInstance;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;

interface DocumentSafeServiceV2 {

    @BindsInstance
    DocumentSafeServiceV2 storeConnection(ExtendedStoreConnection storeConnection);

    DocumentSafeService build();
}
