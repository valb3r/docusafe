package org.adorsys.documentsafe.layer00common.complextypes;

import org.adorsys.documentsafe.layer00common.basetypes.DocumentBucketName;
import org.adorsys.documentsafe.layer00common.basetypes.DocumentID;
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
