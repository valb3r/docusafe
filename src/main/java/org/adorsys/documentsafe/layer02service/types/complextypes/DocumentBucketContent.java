package org.adorsys.documentsafe.layer02service.types.complextypes;

import org.adorsys.documentsafe.layer02service.types.DocumentBucketName;
import org.adorsys.documentsafe.layer02service.types.DocumentID;

import java.util.List;

/**
 * Created by peter on 15.01.18.
 */
public class DocumentBucketContent {
    private DocumentBucketName documentBucketName;
    private List<DocumentBucketContent> subBuckets;
    private List<DocumentID> documents;
}
