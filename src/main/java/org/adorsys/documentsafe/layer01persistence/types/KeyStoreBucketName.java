package org.adorsys.documentsafe.layer01persistence.types;

import org.adorsys.documentsafe.layer00common.basetypes.BaseTypeString;

/**
 * Created by peter on 06.01.18.
 */
public class KeyStoreBucketName extends BaseTypeString {
    public static final String BUCKET_SEPARATOR = "@KS@";

    public KeyStoreBucketName(String value) {
        super(value);
    }
}
