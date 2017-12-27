package org.adorsys.resource.server.basetypes.adapter;

import org.adorsys.resource.server.basetypes.KeyStoreName;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on 23.12.2017 at 18:20:45.
 */
public class KeyStoreNameRestAdapter extends XmlAdapter<String, KeyStoreName> {
    @Override
    public KeyStoreName unmarshal(String value) {
        return new KeyStoreName(value);
    }

    @Override
    public String marshal(KeyStoreName value) {
        return (value != null) ? value.getValue() : null;
    }
}
