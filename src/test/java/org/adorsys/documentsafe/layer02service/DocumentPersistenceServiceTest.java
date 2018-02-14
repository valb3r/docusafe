package org.adorsys.documentsafe.layer02service;

import org.adorsys.documentsafe.layer02service.impl.DocumentPersistenceServiceImpl;
import org.adorsys.documentsafe.layer02service.types.DocumentContent;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentBucketPath;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentContentWithContentMetaInfo;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKeyAndAccessType;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.encobject.service.ExtendedStoreConnection;
import org.adorsys.encobject.types.OverwriteFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by peter on 02.01.18.
 */
public class DocumentPersistenceServiceTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentPersistenceServiceTest.class);

    private ExtendedStoreConnection extendedStoreConnection;
    private Set<DocumentBucketPath> createdBuckets = new HashSet<>();

    public DocumentPersistenceServiceTest(ExtendedStoreConnection extendedStoreConnection) {
        this.extendedStoreConnection = extendedStoreConnection;
    }

    public DocumentStuff testPersistDocument(DocumentGuardService documentGuardService,
                                             DocumentBucketPath documentBucketPath,
                                             DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType,
                                             DocumentContent documentContent) {
        return testPersistDocument(documentGuardService, documentBucketPath, documentKeyIDWithKeyAndAccessType, documentContent, OverwriteFlag.FALSE);
    }
    public DocumentStuff testPersistDocument(DocumentGuardService documentGuardService,
                                             DocumentBucketPath documentBucketPath,
                                             DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType,
                                             DocumentContent documentContent,
                                             OverwriteFlag overwriteFlag) {
        DocumentPersistenceService documentPersistenceService = new DocumentPersistenceServiceImpl(extendedStoreConnection);
        documentPersistenceService.persistDocument(
                documentKeyIDWithKeyAndAccessType.getDocumentKeyIDWithKey(),
                documentBucketPath,
                documentContent,
                overwriteFlag,
                null);
        createdBuckets.add(documentBucketPath);
        AllServiceTest.buckets.add(documentBucketPath.getBucketDirectory());
        return new DocumentStuff(documentBucketPath);
    }

    public DocumentContentWithContentMetaInfo testLoadDocument(DocumentGuardService documentGuardService,
                                 KeyStoreAccess keyStoreAccess,
                                 DocumentBucketPath documentBucketPath) {
        DocumentPersistenceService documentPersistenceService = new DocumentPersistenceServiceImpl(extendedStoreConnection);
        DocumentContentWithContentMetaInfo readContent = documentPersistenceService.loadDocument(
                keyStoreAccess,
                documentBucketPath);
        LOGGER.debug("Gelesenes Document enthält:" + readContent + " bzw " + new String(readContent.getDocumentContent().getValue()));
        return readContent;
    }

    public static class DocumentStuff {
        public final DocumentBucketPath documentBucketPath;

        public DocumentStuff(DocumentBucketPath documentBucketPath) {
            this.documentBucketPath = documentBucketPath;
        }
    }
}
