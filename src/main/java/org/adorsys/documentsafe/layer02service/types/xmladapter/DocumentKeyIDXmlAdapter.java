package org.adorsys.documentsafe.layer02service.types.xmladapter;

import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on 12.01.2018 at 15:37:31.
 */
public class DocumentKeyIDXmlAdapter extends XmlAdapter<String, DocumentKeyID> {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentKeyIDXmlAdapter.class);
    @Override
    public DocumentKeyID unmarshal(String value) {
        return new DocumentKeyID(value);
    }

    @Override
    public String marshal(DocumentKeyID value) {
        return (value != null) ? value.getValue() : null;
    }
}
