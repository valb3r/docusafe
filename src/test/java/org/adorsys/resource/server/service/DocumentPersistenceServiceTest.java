package org.adorsys.resource.server.service;

import org.adorsys.encobject.service.BlobStoreConnection;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.utils.TestFsBlobStoreFactory;
import org.adorsys.resource.server.persistence.basetypes.DocumentContent;
import org.adorsys.resource.server.persistence.basetypes.DocumentID;
import org.adorsys.resource.server.persistence.ExtendedObjectPersistence;
import org.adorsys.resource.server.persistence.basetypes.DocumentBucketName;
import org.adorsys.resource.server.persistence.complextypes.DocumentGuardLocation;
import org.adorsys.resource.server.persistence.complextypes.DocumentLocation;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreAuth;
import org.adorsys.resource.server.persistence.complextypes.KeyStoreLocation;
import org.junit.Assert;

/**
 * Created by peter on 02.01.18.
 */
public class DocumentPersistenceServiceTest {
    private static BlobStoreContextFactory documentContextFactory;
    private static ExtendedObjectPersistence documentExtendedPersistence;
    private static ContainerPersistence containerPersistence;

    private DocumentBucketName documentBucketName = new DocumentBucketName("document-bucket");
    private DocumentID documentID = new DocumentID("document-id-123");
    private DocumentContent documentContent = new DocumentContent("Der Inhalt ist ein Affe".getBytes());


    public static void beforeClass() {
        documentContextFactory = new TestFsBlobStoreFactory();
        BlobStoreConnection blobStoreConnection = new BlobStoreConnection(documentContextFactory);
        documentExtendedPersistence = new ExtendedObjectPersistence(blobStoreConnection);
        containerPersistence = new ContainerPersistence(blobStoreConnection);
    }

    public static void afterClass() {

    }

    public DocumentStuff testPersistDocument(DocumentGuardService documentGuardService,
                                             KeyStoreAuth keyStoreAuth,
                                             DocumentGuardLocation documentGuardLocation) {
        DocumentPersistenceService documentPersistenceService = new DocumentPersistenceService(containerPersistence, documentExtendedPersistence, documentGuardService);
        DocumentLocation documentLocation = documentPersistenceService.persistDocument(
                keyStoreAuth,
                documentGuardLocation,
                documentBucketName,
                documentID,
                documentContent);
        return new DocumentStuff(documentLocation);
    }

    public void testLoadDocument(DocumentGuardService documentGuardService,
                                 KeyStoreAuth keyStoreAuth,
                                 KeyStoreLocation keyStoreLocation,
                                 DocumentLocation documentLocation) {
        DocumentPersistenceService documentPersistenceService = new DocumentPersistenceService(containerPersistence, documentExtendedPersistence, documentGuardService);
        DocumentContent readContent = documentPersistenceService.loadDocument(
                keyStoreLocation,
                keyStoreAuth,
                documentLocation);
        Assert.assertEquals("Content of Document", this.documentContent.toString(), readContent.toString());
        System.out.println("Gelesenes Document enthält:" + readContent + " bzw " + new String(readContent.getValue()));
    }

    public static class DocumentStuff {
        public DocumentLocation documentLocation;

        public DocumentStuff(DocumentLocation documentLocation) {
            this.documentLocation = documentLocation;
        }
    }
}
