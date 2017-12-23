package org.adorsys.resource.server.basetypes.adapter;

import org.adorsys.resource.server.basetypes.EncryptedDocumentContent;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on 23.12.2017 at 17:43:53.
 */
public class EncryptedDocumentContentRestAdapter extends XmlAdapter<byte[], EncryptedDocumentContent> {
    @Override
    public EncryptedDocumentContent unmarshal(byte[] value) {
        return new EncryptedDocumentContent(value);
    }

    @Override
    public byte[] marshal(EncryptedDocumentContent value) {
        return (value != null) ? value.getValue() : null;
    }
}
