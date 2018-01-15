package org.adorsys.documentsafe.layer02service.types;

import org.adorsys.documentsafe.layer00common.basetypes.BaseTypeString;
import org.adorsys.documentsafe.layer02service.types.xmladapter.DocumentKeyIDXmlAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by peter on 23.12.2017 at 17:50:49.
 * Gehört immer zu einem DocumentKey, siehe auch DocumentKeyIDWithKey
 */
@XmlJavaTypeAdapter(DocumentKeyIDXmlAdapter.class)
public class DocumentKeyID extends BaseTypeString {
    public DocumentKeyID() {
    }

    public DocumentKeyID(String value) {
        super(value);
    }
}
