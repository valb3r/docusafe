package org.adorsys.documentsafe.layer02service.types.complextypes;

import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Created by peter on 06.01.18.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class DocumentBucketPath extends BucketPath {
    public DocumentBucketPath(String value) {
        super(value);
    }

    public DocumentBucketPath(BucketPath bucketPath) {
        super(bucketPath);
    }
}
