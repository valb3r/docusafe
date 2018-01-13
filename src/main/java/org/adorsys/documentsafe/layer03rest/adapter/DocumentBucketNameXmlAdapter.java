package org.adorsys.documentsafe.layer03rest.adapter;

import org.adorsys.documentsafe.layer02service.types.DocumentBucketName;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on 13.01.2018 at 20:35:59.
 */

public class DocumentBucketNameXmlAdapter extends XmlAdapter<String, DocumentBucketName> {
    @Override
    public DocumentBucketName unmarshal(String value) {
        return new DocumentBucketName(value);
    }

    @Override
    public String marshal(DocumentBucketName value) {
        return (value != null) ? value.getValue() : null;
    }
}
