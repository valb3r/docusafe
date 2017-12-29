package org.adorsys.resource.server.basetypes.adapter;

import org.adorsys.resource.server.basetypes.GuardKey;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on 29.12.2017 at 14:11:37.
 */
public class GuardKeyRestAdapter extends XmlAdapter<byte[], GuardKey> {
    @Override
    public GuardKey unmarshal(byte[] value) {
        return new GuardKey(value);
    }

    @Override
    public byte[] marshal(GuardKey value) {
        return (value != null) ? value.getValue() : null;
    }
}
