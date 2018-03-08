package org.adorsys.encobject.filesystem;

import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.exceptions.StorageConnectionException;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.FileSystemException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by peter on 26.02.18 at 12:34.
 */
public class ExtendedZipFileHelper extends ZipFileHelper {
    public ExtendedZipFileHelper(BucketDirectory basedir) {
        super(basedir);
    }

    public StorageMetadata plainReadZipMetadataOnly(BucketPath bucketPath) {
        try {
            File file = BucketPathFileHelper.getAsFile(baseDir.append(bucketPath.add(ZIP_SUFFIX)));
            if (!file.exists()) {
                throw new FileSystemException("File does not exist" + bucketPath);
            }

            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
            ZipEntry entry;
            String jsonString = null;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(ZIP_STORAGE_METADATA_JSON)) {
                    jsonString = new String(IOUtils.toByteArray(zis));
                }
                zis.closeEntry();
            }
            if (jsonString == null) {
                throw new StorageConnectionException("Zipfile " + bucketPath + " does not have entry for " + ZIP_STORAGE_METADATA_JSON);
            }

            StorageMetadata storageMetadata = gsonHelper.fromJson(jsonString);
            return storageMetadata;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
