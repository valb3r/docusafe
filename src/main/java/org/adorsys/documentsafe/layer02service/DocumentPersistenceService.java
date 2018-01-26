package org.adorsys.documentsafe.layer02service;

import org.adorsys.documentsafe.layer01persistence.types.OverwriteFlag;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentBucketPath;
import org.adorsys.documentsafe.layer02service.types.DocumentContent;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentContentWithContentMetaInfo;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.encobject.domain.ContentMetaInfo;

/**
 * Created by peter on 11.01.18.
 */
public interface DocumentPersistenceService {
    void persistDocument(
            DocumentKeyIDWithKey documentKeyIDWithKey,
            DocumentBucketPath documentBucketPath,
            DocumentContent documentContent,
            OverwriteFlag overwriteFlag,
            ContentMetaInfo contentMetaInfo);

    DocumentContentWithContentMetaInfo loadDocument(
            KeyStoreAccess keyStoreAccess,
            DocumentBucketPath documentBucketPath);
}
