package org.adorsys.documentsafe.layer02service.types.complextypes;

import org.adorsys.documentsafe.layer00common.exceptions.BaseException;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.adorsys.encobject.domain.ObjectHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by peter on 06.01.18.
 */
public class DocumentBucketPath extends BucketPath {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentBucketPath.class);
    public DocumentBucketPath() {
    }

    public DocumentBucketPath(String value) {
        super(value);
    }

    public DocumentBucketPath(BucketPath bucketPath) {
        super(bucketPath);
    }

    public DocumentBucketPath(String container, String path) {
        super(container, path);
    }


    public DocumentDirectory getDocumentDirectory() {
        ObjectHandle objectHandle = getObjectHandle();
        String name = objectHandle.getName();
        if (name == null) {
            throw new BaseException("name must not be null");
        }
        DocumentDirectory documentDirectory = new DocumentDirectory(new BucketPath(this.getObjectHandle().getContainer()));
        String directory = getDirectoryOf(name);
        if (directory != null) {
            documentDirectory = new DocumentDirectory(documentDirectory.append(directory));
        }
        LOGGER.debug("--->directory for path : " + documentDirectory + " for " + this);
        return documentDirectory;
    }

    private static String getDirectoryOf(String value) {
        int i = value.lastIndexOf(BucketPath.BUCKET_SEPARATOR);
        if (i == -1) {
            return null;
        }
        return value.substring(0, i);
    }



}
