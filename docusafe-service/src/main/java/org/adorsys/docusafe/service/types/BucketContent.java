package org.adorsys.docusafe.service.types;

import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.StorageMetadata;

import java.util.List;

/**
 * Created by peter on 14.06.18 at 10:45.
 */
public interface BucketContent {
    BucketDirectory getBucketDirectory();
    List<BucketPath> getFiles();
    List<BucketDirectory> getSubdirectories();
    List<StorageMetadata> getContent();
}
