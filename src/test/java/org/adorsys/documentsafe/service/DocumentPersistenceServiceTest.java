package org.adorsys.documentsafe.service;

import org.adorsys.documentsafe.layer00common.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer02service.DocumentGuardService;
import org.adorsys.documentsafe.layer02service.DocumentPersistenceService;
import org.adorsys.documentsafe.layer02service.types.DocumentBucketName;
import org.adorsys.documentsafe.layer02service.types.DocumentContent;
import org.adorsys.documentsafe.layer02service.types.DocumentID;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentLocation;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.encobject.service.BlobStoreConnection;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.utils.TestFsBlobStoreFactory;
import org.adorsys.documentsafe.layer01persistence.ExtendedObjectPersistence;
import org.junit.Assert;
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
    private static Set<DocumentBucketName> createdBuckets;

    private DocumentBucketName documentBucketName = new DocumentBucketName("document-bucket");
    private DocumentID documentID = new DocumentID("document-id-123");
    private DocumentContent documentContent = new DocumentContent("Der Inhalt ist ein Affe".getBytes());


    public static void beforeClass() {
        BlobStoreConnection blobStoreConnection = new BlobStoreConnection(new TestFsBlobStoreFactory());
        documentExtendedPersistence = new ExtendedObjectPersistence(blobStoreConnection);
        containerPersistence = new ContainerPersistence(blobStoreConnection);
        createdBuckets = new HashSet<>();
    }

    public static void afterClass() {
        try {
            for (DocumentBucketName bucket : createdBuckets) {
                containerPersistence.deleteContainer(bucket.getValue());
            }
        } catch(Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public DocumentStuff testPersistDocument(DocumentGuardService documentGuardService,
                                             DocumentKeyIDWithKey documentKeyIDWithKey) {
        DocumentPersistenceService documentPersistenceService = new DocumentPersistenceService(containerPersistence, documentExtendedPersistence, documentGuardService);
        DocumentLocation documentLocation = documentPersistenceService.persistDocument(
                documentKeyIDWithKey,
                documentBucketName,
                documentID,
                documentContent);
        createdBuckets.add(documentBucketName);
        return new DocumentStuff(documentLocation);
    }

    public void testLoadDocument(DocumentGuardService documentGuardService,
                                 KeyStoreAccess keyStoreAccess,
                                 DocumentLocation documentLocation) {
        DocumentPersistenceService documentPersistenceService = new DocumentPersistenceService(containerPersistence, documentExtendedPersistence, documentGuardService);
        DocumentContent readContent = documentPersistenceService.loadDocument(
                keyStoreAccess,
                documentLocation);
        Assert.assertEquals("Content of Document", this.documentContent.toString(), readContent.toString());
        LOGGER.info("Gelesenes Document enthält:" + readContent + " bzw " + new String(readContent.getValue()));
    }

    public static class DocumentStuff {
        public DocumentLocation documentLocation;

        public DocumentStuff(DocumentLocation documentLocation) {
            this.documentLocation = documentLocation;
        }
    }
}
