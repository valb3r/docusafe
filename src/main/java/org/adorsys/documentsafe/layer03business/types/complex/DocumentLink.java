package org.adorsys.documentsafe.layer03business.types.complex;

import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentBucketPath;

/**
 * Created by peter on 23.01.18 at 17:19.
 */
public class DocumentLink {
    private DocumentBucketPath sourceDocumentLocation;
    private DocumentBucketPath destinationDocumentLocation;

    public DocumentLink(DocumentBucketPath sourceDocumentLocation, DocumentBucketPath destinationDocumentLocation) {
        this.sourceDocumentLocation = sourceDocumentLocation;
        this.destinationDocumentLocation = destinationDocumentLocation;
    }

    public DocumentBucketPath getSourceDocumentBucketPath() {
        return sourceDocumentLocation;
    }

    public DocumentBucketPath getDestinationDocumentBucketPath() {
        return destinationDocumentLocation;
    }
}
