package org.adorsys.resource.server.basetypes.adapter;

import org.adorsys.resource.server.basetypes.DocumnentKeyID;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on 23.12.2017 at 17:50:49.
 */
public class KeyIDRestAdapter extends XmlAdapter<String, DocumnentKeyID> {
    @Override
    public DocumnentKeyID unmarshal(String value) {
        return new DocumnentKeyID(value);
    }

    @Override
    public String marshal(DocumnentKeyID value) {
        return (value != null) ? value.getValue() : null;
    }
}
