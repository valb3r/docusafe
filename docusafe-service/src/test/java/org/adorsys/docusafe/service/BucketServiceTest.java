package org.adorsys.docusafe.service;

import org.adorsys.docusafe.service.impl.BucketServiceImpl;
import org.adorsys.docusafe.service.types.BucketContent;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.docusafe.service.types.complextypes.DocumentBucketPath;
import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.adorsys.encobject.types.OverwriteFlag;

/**
 * Created by peter on 17.01.18 at 16:51.
 */
public class BucketServiceTest {
    private BucketService bucketService;

    public BucketServiceTest(ExtendedStoreConnection extendedStoreConnection) {
        bucketService = new BucketServiceImpl(extendedStoreConnection);
    }

    public void createBucket(BucketDirectory bucketDirectory) {
        bucketService.createBucket(bucketDirectory);
    }

    public BucketContent listBucket(BucketDirectory bucketDirectory, ListRecursiveFlag listRecursiveFlag) {
        return bucketService.readDocumentBucket(bucketDirectory, listRecursiveFlag);
    }

    public boolean bucketExists(BucketDirectory bucketDirectory) {
        return bucketService.bucketExists(bucketDirectory);
    }
    public boolean fileExists(BucketPath bucketPath) {
        return bucketService.fileExists(bucketPath);
    }

    public void createFiles(KeyStoreServiceTest.KeyStoreStuff keyStoreStuff, ExtendedStoreConnection extendedStoreConnection, BucketDirectory rootDirectory, int subdirs, int subfiles) {
        createFilesAndFoldersRecursivly(keyStoreStuff, rootDirectory, subdirs, subfiles, 3, extendedStoreConnection);
    }

    private void createFilesAndFoldersRecursivly(KeyStoreServiceTest.KeyStoreStuff keyStoreStuff, BucketDirectory rootDirectory, int subdirs, int subfiles, int depth , ExtendedStoreConnection extendedStoreConnection) {
        if (depth == 0) {
            return;
        }
        DocumentContent documentContent = new DocumentContent("Affe".getBytes());
        DocumentGuardServiceTest documentGuardServiceTest = new DocumentGuardServiceTest(extendedStoreConnection);
        DocumentKeyIDWithKey documentKeyIDWithKey = documentGuardServiceTest.createKeyIDWithKey();
        DocumentPersistenceServiceTest documentPersistenceServiceTest = new DocumentPersistenceServiceTest(extendedStoreConnection);

        for (int i = 0; i<subfiles; i++) {
            documentPersistenceServiceTest.testPersistDocument(keyStoreStuff, new DocumentBucketPath(rootDirectory.appendName("file").add("" + i)),
                    documentContent, OverwriteFlag.FALSE);

        }
        for (int i = 0; i<subdirs; i++) {
            createFilesAndFoldersRecursivly(keyStoreStuff, rootDirectory.appendDirectory("subdir" + i), subdirs, subfiles, depth-1, extendedStoreConnection);
        }
    }

}
