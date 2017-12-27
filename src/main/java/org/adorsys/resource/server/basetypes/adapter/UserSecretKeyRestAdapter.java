package org.adorsys.resource.server.basetypes.adapter;

import org.adorsys.resource.server.basetypes.UserSecretKey;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on 23.12.2017 at 17:49:02.
 */
public class UserSecretKeyRestAdapter extends XmlAdapter<byte[], UserSecretKey> {
    @Override
    public UserSecretKey unmarshal(byte[] value) {
        return new UserSecretKey(value);
    }

    @Override
    public byte[] marshal(UserSecretKey value) {
        return (value != null) ? value.getValue() : null;
    }
}
