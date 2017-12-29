package org.adorsys.resource.server.basetypes;

import org.adorsys.resource.server.basetypes.adapter.GuardKeyRestAdapter;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by peter on 29.12.2017 at 14:11:37.
 */
@XmlJavaTypeAdapter(GuardKeyRestAdapter.class)
@XmlType
public class GuardKey extends BaseTypeByteArray {
    public GuardKey() {}

    public GuardKey(byte[] value) {
        super(value);
    }
}
