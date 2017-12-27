package org.adorsys.resource.server.basetypes;

import org.adorsys.resource.server.basetypes.adapter.EncryptedDocumentContentRestAdapter;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by peter on 23.12.2017 at 17:43:53.
 */
@XmlJavaTypeAdapter(EncryptedDocumentContentRestAdapter.class)
@XmlType
public class EncryptedDocumentContent extends BaseTypeByteArray {
    public EncryptedDocumentContent() {}

    public EncryptedDocumentContent(byte[] value) {
        super(value);
    }
}
