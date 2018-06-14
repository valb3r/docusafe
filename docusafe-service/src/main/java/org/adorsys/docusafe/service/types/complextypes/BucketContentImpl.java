package org.adorsys.docusafe.service.types.complextypes;

import org.adorsys.docusafe.service.types.BucketContent;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.StorageType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 15.01.18.
 */
public class BucketContentImpl implements BucketContent {
    private final BucketDirectory bucketDirectory;
    private final List<StorageMetadata> content;

    public BucketContentImpl(BucketDirectory bucketDirectory, List<StorageMetadata> content) {
        this.bucketDirectory = bucketDirectory;
        this.content = content;
    }

    @Override
    public BucketDirectory getBucketDirectory() {
        return bucketDirectory;
    }

    @Override
    public List<BucketPath> getFiles() {
        List<BucketPath> files = new ArrayList<>();
        content.forEach(metadata -> {
            if (metadata.getType().equals(StorageType.BLOB)) {
                files.add(new BucketPath(metadata.getName()));
            }
        });
        return files;
    }

    @Override
    public List<BucketDirectory> getSubdirectories() {
        List<BucketDirectory> dirs = new ArrayList<>();
        content.forEach(metadata -> {
            if (metadata.getType().equals(StorageType.FOLDER)) {
                dirs.add(new BucketDirectory(metadata.getName()));
            }
        });
        return dirs;
    }

    public List<StorageMetadata> getContent() {
        return content;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("unstripped\n");
        sb.append("BucketContentImpl " + bucketDirectory + "(" + content.size() + "){");
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
