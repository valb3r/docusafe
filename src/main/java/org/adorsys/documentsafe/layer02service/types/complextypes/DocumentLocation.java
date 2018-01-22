package org.adorsys.documentsafe.layer02service.types.complextypes;

import org.adorsys.documentsafe.layer02service.types.DocumentID;
import org.adorsys.documentsafe.layer01persistence.LocationInterface;
import org.adorsys.encobject.domain.ObjectHandle;


/**
 * Created by peter on 06.01.18.
 */
public class DocumentLocation implements LocationInterface {
    private final DocumentID documentID;
    private final DocumentBucketPath documentBucketPath;
    public DocumentLocation(DocumentID documentID, DocumentBucketPath documentBucketPath) {
        this.documentID = documentID;
        this.documentBucketPath = documentBucketPath;
    }

    public ObjectHandle getLocationHandle() {
        return new ObjectHandle(documentBucketPath.getFirstBucket().getValue(), documentBucketPath.getSubBuckets() + documentID.getValue());
    }

    public DocumentID getDocumentID() {
        return documentID;
    }

    public DocumentBucketPath getDocumentBucketPath() {
        return documentBucketPath;
    }

    @Override
    public String toString() {
        return "DocumentLocation{" +
                "documentID=" + documentID +
                ", documentBucketPath=" + documentBucketPath +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocumentLocation that = (DocumentLocation) o;

        if (documentID != null ? !documentID.equals(that.documentID) : that.documentID != null) return false;
        return documentBucketPath != null ? documentBucketPath.equals(that.documentBucketPath) : that.documentBucketPath == null;

    }

    @Override
    public int hashCode() {
        int result = documentID != null ? documentID.hashCode() : 0;
        result = 31 * result + (documentBucketPath != null ? documentBucketPath.hashCode() : 0);
        return result;
    }
}
