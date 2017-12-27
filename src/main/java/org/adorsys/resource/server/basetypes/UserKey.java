package org.adorsys.resource.server.basetypes;

import org.adorsys.resource.server.basetypes.adapter.UserKeyRestAdapter;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by peter on 23.12.2017 at 17:56:41.
 */
@XmlJavaTypeAdapter(UserKeyRestAdapter.class)
@XmlType
public class UserKey extends BaseTypeByteArray {
    public UserKey() {}

    public UserKey(byte[] value) {
        super(value);
    }
}
