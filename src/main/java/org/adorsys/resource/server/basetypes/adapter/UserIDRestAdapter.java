package org.adorsys.resource.server.basetypes.adapter;

import org.adorsys.resource.server.basetypes.UserID;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on 23.12.2017 at 17:22:42.
 */
public class UserIDRestAdapter extends XmlAdapter<String, UserID> {
    @Override
    public UserID unmarshal(String value) {
        return new UserID(value);
    }

    @Override
    public String marshal(UserID value) {
        return (value != null) ? value.getValue() : null;
    }
}
