package org.adorsys.documentsafe.layer02service.types.complextypes;

import org.adorsys.documentsafe.layer02service.impl.ExtendedStorageMetadata;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.PageSet;
import org.adorsys.encobject.domain.StorageMetadata;

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
        sb.append("unstripped\n");
        sb.append("BucketContent " + bucketPath + "(" + content.size() + "){");
        for (StorageMetadata meta : getOriginalContent()) {
            sb.append("\n\t[");
            sb.append(meta.getName());
            sb.append("] ");
        }
        sb.append("}");
        sb.append("stripped\n");
        sb.append("BucketContent " + bucketPath + "(" + content.size() + "){");
        for (StorageMetadata meta : getStrippedContent()) {
            sb.append("\n\t[");
            sb.append(meta.getName());
            sb.append("] ");
        }
        sb.append("}");
        return sb.toString();
    }
}
