package org.adorsys.resource.server.basetypes.adapter;

import org.adorsys.resource.server.basetypes.DocKeyID;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on 23.12.2017 at 17:50:49.
 */
public class KeyIDRestAdapter extends XmlAdapter<String, DocKeyID> {
    @Override
    public DocKeyID unmarshal(String value) {
        return new DocKeyID(value);
    }

    @Override
    public String marshal(DocKeyID value) {
        return (value != null) ? value.getValue() : null;
    }
}
