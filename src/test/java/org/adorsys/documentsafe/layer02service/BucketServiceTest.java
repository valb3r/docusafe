package org.adorsys.documentsafe.layer02service;

import org.adorsys.documentsafe.layer02service.impl.BucketServiceImpl;
import org.adorsys.documentsafe.layer02service.types.DocumentContent;
import org.adorsys.documentsafe.layer02service.types.complextypes.BucketContent;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentBucketPath;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.adorsys.encobject.types.OverwriteFlag;

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

    public BucketContent listBucket(BucketPath bucketPath, ListRecursiveFlag listRecursiveFlag) {
        return bucketService.readDocumentBucket(bucketPath, listRecursiveFlag);
    }

    public boolean bucketExists(BucketPath bucketPath) {
        return bucketService.bucketExists(bucketPath);
    }

    public void createFiles(BlobStoreContextFactory factory, BucketPath rootPath, int subdirs, int subfiles) {
        createFilesAndFoldersRecursivly(rootPath, subdirs, subfiles, 3, factory);
    }

    private void createFilesAndFoldersRecursivly(BucketPath rootPath, int subdirs, int subfiles, int depth , BlobStoreContextFactory factory) {
        if (depth == 0) {
            return;
        }
        DocumentContent documentContent = new DocumentContent("Affe".getBytes());
        DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest(factory);
        DocumentKeyIDWithKey keyIDWithKey = documentGuardServiceTest.createKeyIDWithKey();
        DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest(factory);

        for (int i = 0; i<subfiles; i++) {
            documentPersistenceServiceTest.testPersistDocument(null, new DocumentBucketPath(rootPath.append("file").add("" + i)),
                    keyIDWithKey, documentContent, OverwriteFlag.FALSE);

        }
        for (int i = 0; i<subdirs; i++) {
            createFilesAndFoldersRecursivly(rootPath.append("subdir").add("" + i), subdirs, subfiles, depth-1, factory);
        }
    }

}
