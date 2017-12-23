package org.adorsys.resource.server.basetypes;

import org.adorsys.resource.server.basetypes.adapter.KeyStoreNameRestAdapter;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by peter on 23.12.2017 at 18:20:45.
 */
@XmlJavaTypeAdapter(KeyStoreNameRestAdapter.class)
@XmlType
public class KeyStoreName extends BaseTypeString {
    public KeyStoreName() {}

    public KeyStoreName(String value) {
        super(value);
    }
}
