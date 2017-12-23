package org.adorsys.resource.server.basetypes.adapter;

import org.adorsys.resource.server.basetypes.BucketName;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on 23.12.2017 at 18:03:52.
 */
public class BucketNameRestAdapter extends XmlAdapter<String, BucketName> {
    @Override
    public BucketName unmarshal(String value) {
        return new BucketName(value);
    }

    @Override
    public String marshal(BucketName value) {
        return (value != null) ? value.getValue() : null;
    }
}
