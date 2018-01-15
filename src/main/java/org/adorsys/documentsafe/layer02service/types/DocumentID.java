package org.adorsys.documentsafe.layer02service.types;

import org.adorsys.documentsafe.layer00common.basetypes.BaseTypeString;
import org.adorsys.documentsafe.layer02service.types.xmladapter.DocumentIDXmlAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by peter on 23.12.2017 at 17:22:35.
 */
@XmlJavaTypeAdapter(DocumentIDXmlAdapter.class)
public class DocumentID extends BaseTypeString {
    public DocumentID() {}

    public DocumentID(String value) {
        super(value);
    }
}
