package org.adorsys.resource.server.basetypes.adapter;

import org.adorsys.resource.server.basetypes.DocumentContent;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on 23.12.2017 at 17:39:26.
 */
public class DocumentContentRestAdapter extends XmlAdapter<byte[], DocumentContent> {
    @Override
    public DocumentContent unmarshal(byte[] value) {
        return new DocumentContent(value);
    }

    @Override
    public byte[] marshal(DocumentContent value) {
        return (value != null) ? value.getValue() : null;
    }
}
