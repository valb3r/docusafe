package org.adorsys.resource.server.basetypes;

import org.adorsys.resource.server.basetypes.adapter.UserPublicKeyRestAdapter;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by peter on 23.12.2017 at 17:49:10.
 */
@XmlJavaTypeAdapter(UserPublicKeyRestAdapter.class)
@XmlType
public class UserPublicKey extends BaseTypeByteArray {
    public UserPublicKey() {}

    public UserPublicKey(byte[] value) {
        super(value);
    }
}
