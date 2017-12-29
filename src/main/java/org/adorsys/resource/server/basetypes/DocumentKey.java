package org.adorsys.resource.server.basetypes;

import org.adorsys.resource.server.basetypes.adapter.DocumentKeyRestAdapter;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by peter on 29.12.2017 at 13:55:33.
 */
@XmlJavaTypeAdapter(DocumentKeyRestAdapter.class)
@XmlType
public class DocumentKey extends BaseTypeByteArray {
    public DocumentKey() {}

    public DocumentKey(byte[] value) {
        super(value);
    }
}
