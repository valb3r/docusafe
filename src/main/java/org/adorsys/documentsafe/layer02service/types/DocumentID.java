package org.adorsys.documentsafe.layer02service.types;

import com.google.gson.annotations.JsonAdapter;
import org.adorsys.documentsafe.layer00common.basetypes.BaseTypeString;
import org.adorsys.documentsafe.layer03rest.adapter.DocumentIDJsonAdapter;
import org.adorsys.documentsafe.layer03rest.adapter.DocumentIDXmlAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by peter on 23.12.2017 at 17:22:35.
 */
@XmlJavaTypeAdapter(DocumentIDXmlAdapter.class)
@JsonAdapter(DocumentIDJsonAdapter.class)
public class DocumentID extends BaseTypeString {
    public DocumentID() {}

    public DocumentID(String value) {
        super(value);
    }
}
