package org.adorsys.resource.server.basetypes.adapter;

import org.adorsys.resource.server.basetypes.UserPublicKey;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on 23.12.2017 at 17:49:10.
 */
public class UserPublicKeyRestAdapter extends XmlAdapter<byte[], UserPublicKey> {
    @Override
    public UserPublicKey unmarshal(byte[] value) {
        return new UserPublicKey(value);
    }

    @Override
    public byte[] marshal(UserPublicKey value) {
        return (value != null) ? value.getValue() : null;
    }
}
