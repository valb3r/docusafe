package org.adorsys.resource.server.basetypes;

import org.adorsys.resource.server.basetypes.adapter.UserPrivateKeyRestAdapter;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by peter on 23.12.2017 at 17:49:50.
 */
@XmlJavaTypeAdapter(UserPrivateKeyRestAdapter.class)
@XmlType
public class UserPrivateKey extends BaseTypeByteArray {
    public UserPrivateKey() {}

    public UserPrivateKey(byte[] value) {
        super(value);
    }
}
