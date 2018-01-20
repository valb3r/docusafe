package org.adorsys.documentsafe.layer02service.types.complextypes;

import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.adorsys.documentsafe.layer02service.impl.ExtendedStorageMetadata;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 15.01.18.
 */
public class BucketContent {
    private final BucketPath bucketPath;
    private final PageSet<? extends StorageMetadata> content;

    public BucketContent(BucketPath bucketPath, PageSet<? extends StorageMetadata> content) {
        this.bucketPath = bucketPath;
        this.content = content;
    }

    public PageSet<? extends StorageMetadata> getOriginalContent() {
        return content;
    }

    /**
     * Das originale PageSet ist unpraktisch, da im Namen der gesamte Pfad enthalten ist
     * ausschliesslich des buckets selbst
     * list for /bucket/a/b/
     * liefert a/b/file1
     * .       a/b/file2
     * .       a/b/c/file1
     * <p>
     * das wird umgewandelt in
     * .       file1
     * .       file2
     * .       c/file1
     */
    public List<ExtendedStorageMetadata> getStrippedContent() {
        List<ExtendedStorageMetadata> list = new ArrayList<>();
        content.forEach(meta -> list.add(new ExtendedStorageMetadata(bucketPath, meta)));
        return list;
    }

    public BucketPath getBucketPath() {
        return bucketPath;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BucketContent " + bucketPath.getObjectHandlePath() + "(" + content.size() + "){");
        for (StorageMetadata meta : getStrippedContent()) {
            sb.append("\n\t[");
            sb.append(meta.getName());
            sb.append("] ");
        }
        sb.append("}");
        return sb.toString();
    }
}
