package org.adorsys.documentsafe.layer02service.complextypes;

import org.adorsys.documentsafe.layer02service.types.DocumentBucketName;
import org.adorsys.documentsafe.layer02service.types.DocumentID;
import org.adorsys.documentsafe.layer01persistence.LocationInterface;
import org.adorsys.encobject.domain.ObjectHandle;

/**
 * Created by peter on 06.01.18.
 */
public class DocumentLocation implements LocationInterface {
    private final DocumentID documentID;
    private final DocumentBucketName documentBucketName;
    public DocumentLocation(DocumentID documentID, DocumentBucketName documentBucketName) {
        this.documentID = documentID;
        this.documentBucketName = documentBucketName;
    }

    public ObjectHandle getLocationHandle() {
        return new ObjectHandle(documentBucketName.getValue(), documentID.getValue());
    }

    @Override
    public String toString() {
        return "DocumentLocation{" +
                "documentID=" + documentID +
                ", documentBucketName=" + documentBucketName +
                '}';
    }

}
