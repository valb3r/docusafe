package org.adorsys.documentsafe.layer02service.types.complextypes;

import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;

/**
 * Created by peter on 15.01.18.
 */
public class BucketContent {
    private final PageSet<? extends StorageMetadata> content;

    public BucketContent(PageSet<? extends StorageMetadata> content) {
        this.content = content;
    }

    public PageSet<? extends StorageMetadata> getContent() {
        return content;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BucketContent{");
        for (StorageMetadata meta : content) {
            sb.append("\t\n[");
            sb.append(meta.getName());
            sb.append("] ");
        }
        sb.append("}");
        return sb.toString();
    }
}
