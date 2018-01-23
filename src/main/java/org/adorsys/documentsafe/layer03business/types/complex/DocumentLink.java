package org.adorsys.documentsafe.layer03business.types.complex;

import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentLocation;

/**
 * Created by peter on 23.01.18 at 17:19.
 */
public class DocumentLink {
    private DocumentLocation sourceDocumentLocation;
    private DocumentLocation destinationDocumentLocation;

    public DocumentLink(DocumentLocation sourceDocumentLocation, DocumentLocation destinationDocumentLocation) {
        this.sourceDocumentLocation = sourceDocumentLocation;
        this.destinationDocumentLocation = destinationDocumentLocation;
    }

    public DocumentLocation getSourceDocumentLocation() {
        return sourceDocumentLocation;
    }

    public DocumentLocation getDestinationDocumentLocation() {
        return destinationDocumentLocation;
    }
}
