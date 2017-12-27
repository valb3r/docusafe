package org.adorsys.resource.server.basetypes;

import org.adorsys.resource.server.basetypes.adapter.UserSecretKeyRestAdapter;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by peter on 23.12.2017 at 17:49:02.
 */
@XmlJavaTypeAdapter(UserSecretKeyRestAdapter.class)
@XmlType
public class UserSecretKey extends BaseTypeByteArray {
    public UserSecretKey() {}

    public UserSecretKey(byte[] value) {
        super(value);
    }
}
