package org.adorsys.documentsafe.layer04rest.types;

import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentBucketPath;
import org.adorsys.documentsafe.layer02service.types.DocumentID;
import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentLocation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by peter on 10.01.18.
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
public class VersionInformation {
    private String info;
    private DocumentKeyID documentKeyID;
    private DocumentLocation documentLocation;

    public VersionInformation() {
    }

    public VersionInformation(String info, DocumentKeyID documentKeyID) {
        this.info = info;
        this.documentKeyID = documentKeyID;
        this.documentLocation = new DocumentLocation(new DocumentID("id"), new DocumentBucketPath("bucket/1/2/3"));

    }

    public String getInfo() {
        return info;
    }

    public DocumentKeyID getDocumentKeyID() {
        return documentKeyID;
    }
}
