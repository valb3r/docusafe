package org.adorsys.resource.server.basetypes;

import org.adorsys.resource.server.persistence.basetypes.BaseTypeByteArray;

/**
 * Created by peter on 23.12.2017 at 17:43:53.
 */
public class EncryptedDocumentContent extends BaseTypeByteArray {
    public EncryptedDocumentContent() {}

    public EncryptedDocumentContent(byte[] value) {
        super(value);
    }
}
