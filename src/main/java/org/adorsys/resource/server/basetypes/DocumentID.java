package org.adorsys.resource.server.basetypes;

import org.adorsys.resource.server.basetypes.adapter.DocumentIDRestAdapter;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by peter on 23.12.2017 at 17:22:35.
 */
@XmlJavaTypeAdapter(DocumentIDRestAdapter.class)
@XmlType
public class DocumentID extends BaseTypeString {
    public DocumentID() {}

    public DocumentID(String value) {
        super(value);
    }
}
