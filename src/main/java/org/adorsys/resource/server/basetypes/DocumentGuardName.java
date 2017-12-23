package org.adorsys.resource.server.basetypes;

import org.adorsys.resource.server.basetypes.adapter.DocumentGuardNameRestAdapter;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by peter on 23.12.2017 at 18:33:14.
 */
@XmlJavaTypeAdapter(DocumentGuardNameRestAdapter.class)
@XmlType
public class DocumentGuardName extends BaseTypeString {
    public DocumentGuardName() {}

    public DocumentGuardName(String value) {
        super(value);
    }
}
