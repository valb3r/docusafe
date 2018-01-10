package org.adorsys.documentsafe.persistence.basetypes;

/**
 * Created by peter on 06.01.18.
 */
public class KeyStoreBucketName extends BaseTypeString {
    public static final String BUCKET_SEPARATOR = "@KS@";

    public KeyStoreBucketName(String value) {
        super(value);
    }
}
