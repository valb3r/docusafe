package org.adorsys.resource.server.basetypes;

import org.adorsys.resource.server.basetypes.adapter.UserIDRestAdapter;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by peter on 23.12.2017 at 17:22:42.
 */
@XmlJavaTypeAdapter(UserIDRestAdapter.class)
@XmlType
public class UserID extends BaseTypeString {
    public UserID() {}

    public UserID(String value) {
        super(value);
    }
}
