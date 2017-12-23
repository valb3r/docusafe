package org.adorsys.resource.server.basetypes.adapter;

import org.adorsys.resource.server.basetypes.DocumentGuardName;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on 23.12.2017 at 18:33:14.
 */
public class DocumentGuardNameRestAdapter extends XmlAdapter<String, DocumentGuardName> {
    @Override
    public DocumentGuardName unmarshal(String value) {
        return new DocumentGuardName(value);
    }

    @Override
    public String marshal(DocumentGuardName value) {
        return (value != null) ? value.getValue() : null;
    }
}
