package org.adorsys.resource.server.basetypes.adapter;

import org.adorsys.resource.server.basetypes.EncryptedKey;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on 23.12.2017 at 18:34:54.
 */
public class EncryptedKeyRestAdapter extends XmlAdapter<byte[], EncryptedKey> {
    @Override
    public EncryptedKey unmarshal(byte[] value) {
        return new EncryptedKey(value);
    }

    @Override
    public byte[] marshal(EncryptedKey value) {
        return (value != null) ? value.getValue() : null;
    }
}
