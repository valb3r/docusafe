package org.adorsys.resource.server.basetypes;

import org.adorsys.resource.server.basetypes.adapter.GuardKeyIDRestAdapter;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by peter on 29.12.2017 at 14:11:52.
 */
@XmlJavaTypeAdapter(GuardKeyIDRestAdapter.class)
@XmlType
public class GuardKeyID extends BaseTypeString {
    public GuardKeyID() {}

    public GuardKeyID(String value) {
        super(value);
    }
}
