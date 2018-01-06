package org.adorsys.resource.server.persistence.complextypes;

import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.resource.server.basetypes.DocumentID;
import org.adorsys.resource.server.persistence.basetypes.DocumentBucketName;

/**
 * Created by peter on 06.01.18.
 */
public class DocumentLocation {
    private final DocumentID documentID;
    private final DocumentBucketName documentBucketName;
    public DocumentLocation(DocumentID documentID, DocumentBucketName documentBucketName) {
        this.documentID = documentID;
        this.documentBucketName = documentBucketName;
    }

    public DocumentID getDocumentID() {
        return documentID;
    }

    public DocumentBucketName getDocumentBucketName() {
        return documentBucketName;
    }

    public ObjectHandle getLocationHanlde() {
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
