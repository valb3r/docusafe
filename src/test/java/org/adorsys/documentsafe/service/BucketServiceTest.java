package org.adorsys.documentsafe.service;

import org.adorsys.documentsafe.layer01persistence.ExtendedBlobStoreConnection;
import org.adorsys.documentsafe.layer01persistence.types.BucketName;
import org.adorsys.documentsafe.layer01persistence.types.OverwriteFlag;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.adorsys.documentsafe.layer02service.BucketService;
import org.adorsys.documentsafe.layer02service.impl.BucketServiceImpl;
import org.adorsys.documentsafe.layer02service.types.DocumentContent;
import org.adorsys.documentsafe.layer02service.types.DocumentID;
import org.adorsys.documentsafe.layer02service.types.complextypes.BucketContent;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentBucketPath;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.encobject.service.BlobStoreConnection;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.adorsys.encobject.utils.TestFsBlobStoreFactory;

/**
 * Created by peter on 17.01.18 at 16:51.
 */
public class BucketServiceTest {
    private BucketService bucketService;

    public BucketServiceTest(BlobStoreContextFactory factory) {
        bucketService = new BucketServiceImpl(factory);
    }

    public void createBucket(BucketPath bucketPath) {
        bucketService.createBucket(bucketPath);
    }

    public BucketContent listBucket(BucketPath bucketPath, boolean recursive) {
        return bucketService.readDocumentBucket(bucketPath, recursive);
    }

    public void createFiles(BlobStoreContextFactory factory, BucketPath rootPath, int subdirs, int subfiles) {
        String[] foldernames = {"bucket", "subbucket", "subsubbucket"};

        DocumentContent documentContent = new DocumentContent("Affe".getBytes());
        BucketServiceTest bucketServiceTest = new BucketServiceTest(factory);
        BlobStoreConnection blobStoreConnection = new ExtendedBlobStoreConnection(new TestFsBlobStoreFactory());
        DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest(factory);
        DocumentKeyIDWithKey keyIDWithKey = documentGuardServiceTest.createKeyIDWithKey();
        DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest(factory);

        for (int folderIndex = 0; folderIndex < foldernames.length; folderIndex++) {
            DocumentBucketPath documentBucketPath = new DocumentBucketPath(rootPath.getObjectHandlePath());

            for (int i = 0; i<folderIndex; i++) {
                documentBucketPath.sub(new BucketName(foldernames[i]));
            }

            for (int j = 0; j<subfiles; j++) {
                DocumentID documentID = new DocumentID("AffenDocument" + j);
                documentPersistenceServiceTest.testPersistDocument(null, documentBucketPath, keyIDWithKey, documentID, documentContent, OverwriteFlag.FALSE);
            }

            documentBucketPath.sub(new BucketName(foldernames[folderIndex]));
            for (int i = 0; i<subdirs; i++) {
                DocumentBucketPath newDocumentBucketPath = new DocumentBucketPath(documentBucketPath.getObjectHandlePath() + i);
                bucketServiceTest.createBucket(newDocumentBucketPath);
                for (int j = 0; j<subfiles; j++) {
                    DocumentID documentID = new DocumentID("AffenDocument" + j);
                    documentPersistenceServiceTest.testPersistDocument(null, newDocumentBucketPath, keyIDWithKey, documentID, documentContent, OverwriteFlag.FALSE);
                }
            }
        }

    }

}
