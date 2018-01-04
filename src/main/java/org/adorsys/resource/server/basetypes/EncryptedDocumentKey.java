package org.adorsys.resource.server.basetypes;

import org.adorsys.resource.server.persistence.basetypes.BaseTypeByteArray;

/**
 * Created by peter on 23.12.2017 at 18:34:54.
 */
public class EncryptedDocumentKey extends BaseTypeByteArray {
    public EncryptedDocumentKey() {}
    public EncryptedDocumentKey(byte[] value) {
        super(value);
    }
}
