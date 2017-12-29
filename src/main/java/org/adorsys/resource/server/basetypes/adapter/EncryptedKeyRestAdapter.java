package org.adorsys.resource.server.basetypes.adapter;

import org.adorsys.resource.server.basetypes.EncryptedDocumentKey;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on 23.12.2017 at 18:34:54.
 */
public class EncryptedKeyRestAdapter extends XmlAdapter<byte[], EncryptedDocumentKey> {
    @Override
    public EncryptedDocumentKey unmarshal(byte[] value) {
        return new EncryptedDocumentKey(value);
    }

    @Override
    public byte[] marshal(EncryptedDocumentKey value) {
        return (value != null) ? value.getValue() : null;
    }
}
