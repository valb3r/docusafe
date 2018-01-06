package org.adorsys.resource.server.service;

import org.adorsys.encobject.service.BlobStoreConnection;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.adorsys.encobject.service.ContainerPersistence;
import org.adorsys.encobject.utils.TestFsBlobStoreFactory;
import org.adorsys.resource.server.basetypes.DocumentContent;
import org.adorsys.resource.server.basetypes.DocumentGuardName;
import org.adorsys.resource.server.basetypes.DocumentID;
import org.adorsys.resource.server.persistence.ExtendedObjectPersistence;
import org.adorsys.resource.server.persistence.PersistentObjectWrapper;
import org.adorsys.resource.server.persistence.basetypes.DocumentBucketName;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreAuth;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreName;
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
                                             DocumentGuardName documentGuardName) {
        DocumentPersistenceService documentPersistenceService = new DocumentPersistenceService(containerPersistence, documentExtendedPersistence, documentGuardService);
        documentPersistenceService.persistDocument(
                keyStoreAuth,
                documentGuardName,
                documentBucketName,
                documentID,
                documentContent);
        return new DocumentStuff(documentBucketName, documentID);
    }

    public void testLoadDocument(DocumentGuardService documentGuardService,
                                 KeyStoreAuth keyStoreAuth,
                                 KeyStoreName keyStoreName,
                                 DocumentBucketName documentBucketName,
                                 DocumentID documentID) {
        DocumentPersistenceService documentPersistenceService = new DocumentPersistenceService(containerPersistence, documentExtendedPersistence, documentGuardService);
        PersistentObjectWrapper persistentObjectWrapper = documentPersistenceService.loadDocument(
                keyStoreName,
                keyStoreAuth,
                documentBucketName,
                documentID);

        DocumentContent readContent = new DocumentContent(persistentObjectWrapper.getData());
        Assert.assertEquals("Content of Document", this.documentContent.toString(), readContent.toString());
        System.out.println("Gelesenes Document enthält:" + readContent + " bzw " + new String(readContent.getValue()));
    }

    public static class DocumentStuff {
        public DocumentBucketName documentBucketName;
        public DocumentID documentID;

        public DocumentStuff(DocumentBucketName documentBucketName, DocumentID documentID) {
            this.documentBucketName = documentBucketName;
            this.documentID = documentID;
        }


    }

}
