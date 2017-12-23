package org.adorsys.resource.server.basetypes.adapter;

import org.adorsys.resource.server.basetypes.DocumentID;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on 23.12.2017 at 17:22:35.
 */
public class DocumentIDRestAdapter extends XmlAdapter<String, DocumentID> {
    @Override
    public DocumentID unmarshal(String value) {
        return new DocumentID(value);
    }

    @Override
    public String marshal(DocumentID value) {
        return (value != null) ? value.getValue() : null;
    }
}
