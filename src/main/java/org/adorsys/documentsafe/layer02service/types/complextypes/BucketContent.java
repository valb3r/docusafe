package org.adorsys.documentsafe.layer02service.types.complextypes;

import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.domain.StorageMetadata;

import java.util.List;

/**
 * Created by peter on 15.01.18.
 */
public class BucketContent {
    private final BucketDirectory bucketDirectory;
    private final List<StorageMetadata> content;

    public BucketContent(BucketDirectory bucketDirectory, List<StorageMetadata> content) {
        this.bucketDirectory = bucketDirectory;
        this.content = content;
    }

    public List<StorageMetadata> getContent() {
        return content;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("unstripped\n");
        sb.append("BucketContent " + bucketDirectory + "(" + content.size() + "){");
        for (StorageMetadata meta : getContent()) {
            sb.append("\n\t[");
            sb.append(meta.getName());
            sb.append(" " );
            sb.append(meta.getType());
            sb.append(" " );
            sb.append(meta.getSize());
            sb.append("] ");
        }
        sb.append("}");
        return sb.toString();
    }
}
