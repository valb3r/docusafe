package org.adorsys.documentsafe.layer02service.types.xmladapter;

import org.adorsys.documentsafe.layer01persistence.types.BucketName;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on 16.01.18.
 */
public class BucketNameXmlAdapter extends XmlAdapter<String, BucketName> {
    @Override
    public BucketName unmarshal(String value) {
        return new BucketName(value);
    }

    @Override
    public String marshal(BucketName value) {
        return (value != null) ? value.getValue() : null;
    }
}
