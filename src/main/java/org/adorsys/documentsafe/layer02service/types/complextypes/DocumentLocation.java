package org.adorsys.documentsafe.layer02service.types.complextypes;

import org.adorsys.documentsafe.layer02service.types.DocumentID;
import org.adorsys.documentsafe.layer01persistence.LocationInterface;
import org.adorsys.encobject.domain.ObjectHandle;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Created by peter on 06.01.18.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class DocumentLocation implements LocationInterface {
    private final DocumentID documentID;
    private final DocumentBucketPath documentBucketPath;
    public DocumentLocation(DocumentID documentID, DocumentBucketPath documentBucketPath) {
        this.documentID = documentID;
        this.documentBucketPath = documentBucketPath;
    }

    public ObjectHandle getLocationHandle() {
        return new ObjectHandle(documentBucketPath.getObjectHandlePath(), documentID.getValue());
    }

    @Override
    public String toString() {
        return "DocumentLocation{" +
                "documentID=" + documentID +
                ", documentBucketPath=" + documentBucketPath +
                '}';
    }

}
