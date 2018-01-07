package org.adorsys.resource.server.persistence.basetypes;

import org.adorsys.resource.server.persistence.basetypes.BaseTypeString;

/**
 * Created by peter on 23.12.2017 at 17:22:35.
 */
public class DocumentID extends BaseTypeString {
    public DocumentID() {}

    public DocumentID(String value) {
        super(value);
    }
}
