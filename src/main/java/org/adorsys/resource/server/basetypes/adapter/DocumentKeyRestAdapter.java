package org.adorsys.resource.server.basetypes.adapter;

import org.adorsys.resource.server.basetypes.DocumentKey;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on 29.12.2017 at 13:55:33.
 */
public class DocumentKeyRestAdapter extends XmlAdapter<byte[], DocumentKey> {
    @Override
    public DocumentKey unmarshal(byte[] value) {
        return new DocumentKey(value);
    }

    @Override
    public byte[] marshal(DocumentKey value) {
        return (value != null) ? value.getValue() : null;
    }
}
