package org.adorsys.documentsafe.layer02service.types.xmladapter;


import org.adorsys.documentsafe.layer02service.types.DocumentID;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on 13.01.2018 at 20:35:53.
 */
public class DocumentIDXmlAdapter extends XmlAdapter<String, DocumentID> {
    @Override
    public DocumentID unmarshal(String value) {
        return new DocumentID(value);
    }

    @Override
    public String marshal(DocumentID value) {
        return (value != null) ? value.getValue() : null;
    }
}
