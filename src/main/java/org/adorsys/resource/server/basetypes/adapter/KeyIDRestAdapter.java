package org.adorsys.resource.server.basetypes.adapter;

import org.adorsys.resource.server.basetypes.KeyID;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on 23.12.2017 at 17:50:49.
 */
public class KeyIDRestAdapter extends XmlAdapter<String, KeyID> {
    @Override
    public KeyID unmarshal(String value) {
        return new KeyID(value);
    }

    @Override
    public String marshal(KeyID value) {
        return (value != null) ? value.getValue() : null;
    }
}
