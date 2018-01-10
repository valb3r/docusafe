package org.adorsys.documentsafe.layer02service.types;

import org.adorsys.documentsafe.layer00common.basetypes.BaseTypeByteArray;

/**
 * Created by peter on 29.12.2017 at 14:11:37.
 */
public class GuardKey extends BaseTypeByteArray {
    public GuardKey() {}

    public GuardKey(byte[] value) {
        super(value);
    }
}
