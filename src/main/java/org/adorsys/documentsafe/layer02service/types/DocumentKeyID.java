package org.adorsys.documentsafe.layer02service.types;

import com.google.gson.annotations.JsonAdapter;
import org.adorsys.documentsafe.layer00common.basetypes.BaseTypeString;
import org.adorsys.documentsafe.layer03rest.adapter.DocumentKeyIDJsonAdapter;
import org.adorsys.documentsafe.layer03rest.adapter.DocumentKeyIDXmlAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by peter on 23.12.2017 at 17:50:49.
 * Gehört immer zu einem DocumentKey, siehe auch DocumentKeyIDWithKey
 */
@XmlJavaTypeAdapter(DocumentKeyIDXmlAdapter.class)
@JsonAdapter(DocumentKeyIDJsonAdapter.class)
public class DocumentKeyID extends BaseTypeString {
    public DocumentKeyID() {
    }

    public DocumentKeyID(String value) {
        super(value);
    }
}
