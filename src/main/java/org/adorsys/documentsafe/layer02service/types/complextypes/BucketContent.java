package org.adorsys.documentsafe.layer02service.types.complextypes;

import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.adorsys.documentsafe.layer02service.types.DocumentID;

import java.util.List;

/**
 * Created by peter on 15.01.18.
 */
public class BucketContent {
    private BucketPath bucketPath;
    private List<BucketContent> subBuckets;
    private List<DocumentID> documents;
}
