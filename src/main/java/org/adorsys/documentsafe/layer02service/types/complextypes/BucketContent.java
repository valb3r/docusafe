package org.adorsys.documentsafe.layer02service.types.complextypes;

import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;

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

    public PageSet<? extends StorageMetadata> getContent() {
        return content;
    }

    public BucketPath getBucketPath() {
        return bucketPath;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BucketContent (" + content.size() + "){");
        for (StorageMetadata meta : content) {
            sb.append("\t\n[");
            sb.append(meta.getName());
            sb.append("] ");
        }
        sb.append("}");
        return sb.toString();
    }
}
