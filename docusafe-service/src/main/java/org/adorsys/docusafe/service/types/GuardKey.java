package org.adorsys.docusafe.service.types;

import org.adorsys.cryptoutils.basetypes.BaseTypeByteArray;

/**
 * Created by peter on 29.12.2017 at 14:11:37.
 */
public class GuardKey extends BaseTypeByteArray {
    public GuardKey() {}

    public GuardKey(byte[] value) {
        super(value);
    }
}
