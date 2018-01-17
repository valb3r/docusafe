package org.adorsys.documentsafe.service;

import org.adorsys.documentsafe.layer01persistence.ExtendedBlobStoreConnection;
import org.adorsys.documentsafe.layer01persistence.ExtendedObjectPersistence;
import org.adorsys.documentsafe.layer01persistence.types.OverwriteFlag;
import org.adorsys.documentsafe.layer02service.DocumentGuardService;
import org.adorsys.documentsafe.layer02service.DocumentPersistenceService;
import org.adorsys.documentsafe.layer02service.impl.DocumentPersistenceServiceImpl;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentBucketPath;
import org.adorsys.documentsafe.layer02service.types.DocumentContent;
import org.adorsys.documentsafe.layer02service.types.DocumentID;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentLocation;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.encobject.service.BlobStoreConnection;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.utils.TestFsBlobStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by peter on 02.01.18.
 */
public class DocumentPersistenceServiceTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentPersistenceServiceTest.class);
    
    private static ExtendedObjectPersistence documentExtendedPersistence;
    private static ContainerPersistence containerPersistence;
    private static Set<DocumentBucketPath> createdBuckets;

    private DocumentID documentID = new DocumentID("document-id-123");


    public static void beforeClass() {
        BlobStoreConnection blobStoreConnection = new ExtendedBlobStoreConnection(new TestFsBlobStoreFactory());
        documentExtendedPersistence = new ExtendedObjectPersistence(blobStoreConnection);
        containerPersistence = new ContainerPersistence(blobStoreConnection);
        createdBuckets = new HashSet<>();
    }

    public static void afterClass() {
        try {
            for (DocumentBucketPath bucket : createdBuckets) {
                containerPersistence.deleteContainer(bucket.getObjectHandlePath());
            }
        } catch(Exception e) {
            // throw BaseExceptionHandler.handle(e);
        }
    }

    public DocumentStuff testPersistDocument(DocumentGuardService documentGuardService,
                                             DocumentBucketPath documentBucketPath,
                                             DocumentKeyIDWithKey documentKeyIDWithKey,
                                             DocumentContent documentContent) {
        return testPersistDocument(documentGuardService, documentBucketPath, documentKeyIDWithKey, documentID, documentContent, OverwriteFlag.FALSE);
    }
    public DocumentStuff testPersistDocument(DocumentGuardService documentGuardService,
                                             DocumentBucketPath documentBucketPath,
                                             DocumentKeyIDWithKey documentKeyIDWithKey,
                                             DocumentID documentID,
                                             DocumentContent documentContent,
                                             OverwriteFlag overwriteFlag) {
        DocumentPersistenceService documentPersistenceService = new DocumentPersistenceServiceImpl(containerPersistence, documentExtendedPersistence, documentGuardService);
        DocumentLocation documentLocation = documentPersistenceService.persistDocument(
                documentKeyIDWithKey,
                documentBucketPath,
                documentID,
                documentContent,
                overwriteFlag);
        createdBuckets.add(documentBucketPath);
        return new DocumentStuff(documentID, documentLocation);
    }

    public DocumentContent testLoadDocument(DocumentGuardService documentGuardService,
                                 KeyStoreAccess keyStoreAccess,
                                 DocumentLocation documentLocation) {
        DocumentPersistenceService documentPersistenceService = new DocumentPersistenceServiceImpl(containerPersistence, documentExtendedPersistence, documentGuardService);
        DocumentContent readContent = documentPersistenceService.loadDocument(
                keyStoreAccess,
                documentLocation);
        LOGGER.info("Gelesenes Document enthält:" + readContent + " bzw " + new String(readContent.getValue()));
        return readContent;
    }

    public static class DocumentStuff {
        public final DocumentID documentID;

        public DocumentStuff(DocumentID documentID, DocumentLocation documentLocation) {
            this.documentID = documentID;
            this.documentLocation = documentLocation;
        }

        public final DocumentLocation documentLocation;

    }
}
