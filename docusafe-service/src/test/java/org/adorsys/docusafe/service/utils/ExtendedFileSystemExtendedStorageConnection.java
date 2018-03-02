package org.adorsys.docusafe.service.utils;

import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.filesystem.FileSystemExtendedStorageConnection;

/**
 * Created by peter on 26.02.18 at 12:29.
 */
public class ExtendedFileSystemExtendedStorageConnection extends FileSystemExtendedStorageConnection {
    public BucketDirectory getBaseDir() {
        return baseDir;
    }

}
