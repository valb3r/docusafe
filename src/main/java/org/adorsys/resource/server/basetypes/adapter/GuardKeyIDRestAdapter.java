package org.adorsys.resource.server.basetypes.adapter;

import org.adorsys.resource.server.basetypes.GuardKeyID;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on 29.12.2017 at 14:11:52.
 */
public class GuardKeyIDRestAdapter extends XmlAdapter<String, GuardKeyID> {
    @Override
    public GuardKeyID unmarshal(String value) {
        return new GuardKeyID(value);
    }

    @Override
    public String marshal(GuardKeyID value) {
        return (value != null) ? value.getValue() : null;
    }
}
