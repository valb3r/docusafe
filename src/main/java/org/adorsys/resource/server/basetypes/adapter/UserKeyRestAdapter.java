package org.adorsys.resource.server.basetypes.adapter;

import org.adorsys.resource.server.basetypes.UserKey;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on 23.12.2017 at 17:56:41.
 */
public class UserKeyRestAdapter extends XmlAdapter<byte[], UserKey> {
    @Override
    public UserKey unmarshal(byte[] value) {
        return new UserKey(value);
    }

    @Override
    public byte[] marshal(UserKey value) {
        return (value != null) ? value.getValue() : null;
    }
}
