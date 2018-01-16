package org.adorsys.documentsafe.service;

import org.adorsys.documentsafe.layer01persistence.ExtendedObjectPersistence;
import org.adorsys.documentsafe.layer02service.InterfaceDocumentGuardService;
import org.adorsys.documentsafe.layer02service.InterfaceDocumentPersistenceService;
import org.adorsys.documentsafe.layer02service.impl.DocumentPersistenceService;
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

    private DocumentBucketPath documentBucketPath = new DocumentBucketPath("document-bucket");
    private DocumentID documentID = new DocumentID("document-id-123");


    public static void beforeClass() {
        BlobStoreConnection blobStoreConnection = new BlobStoreConnection(new TestFsBlobStoreFactory());
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

    public DocumentStuff testPersistDocument(InterfaceDocumentGuardService documentGuardService,
                                             DocumentKeyIDWithKey documentKeyIDWithKey,
                                             DocumentContent documentContent) {
        InterfaceDocumentPersistenceService documentPersistenceService = new DocumentPersistenceService(containerPersistence, documentExtendedPersistence, documentGuardService);
        DocumentLocation documentLocation = documentPersistenceService.persistDocument(
                documentKeyIDWithKey,
                documentBucketPath,
                documentID,
                documentContent);
        createdBuckets.add(documentBucketPath);
        return new DocumentStuff(documentLocation);
    }

    public DocumentContent testLoadDocument(InterfaceDocumentGuardService documentGuardService,
                                 KeyStoreAccess keyStoreAccess,
                                 DocumentLocation documentLocation) {
        InterfaceDocumentPersistenceService documentPersistenceService = new DocumentPersistenceService(containerPersistence, documentExtendedPersistence, documentGuardService);
        DocumentContent readContent = documentPersistenceService.loadDocument(
                keyStoreAccess,
                documentLocation);
        LOGGER.info("Gelesenes Document enthält:" + readContent + " bzw " + new String(readContent.getValue()));
        return readContent;
    }

    public static class DocumentStuff {
        public DocumentLocation documentLocation;

        public DocumentStuff(DocumentLocation documentLocation) {
            this.documentLocation = documentLocation;
        }
    }
}
