package org.adorsys.resource.server.basetypes.adapter;

import org.adorsys.resource.server.basetypes.UserPrivateKey;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on 23.12.2017 at 17:49:50.
 */
public class UserPrivateKeyRestAdapter extends XmlAdapter<byte[], UserPrivateKey> {
    @Override
    public UserPrivateKey unmarshal(byte[] value) {
        return new UserPrivateKey(value);
    }

    @Override
    public byte[] marshal(UserPrivateKey value) {
        return (value != null) ? value.getValue() : null;
    }
}
