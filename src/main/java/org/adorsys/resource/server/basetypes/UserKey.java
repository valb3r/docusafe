package org.adorsys.resource.server.basetypes;

import org.adorsys.resource.server.persistence.basetypes.BaseTypeByteArray;

/**
 * Created by peter on 23.12.2017 at 17:56:41.
 */
public class UserKey extends BaseTypeByteArray {
    public UserKey() {}

    public UserKey(byte[] value) {
        super(value);
    }
}
