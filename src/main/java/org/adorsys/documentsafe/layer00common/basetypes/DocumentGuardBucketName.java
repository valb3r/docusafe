package org.adorsys.documentsafe.layer00common.basetypes;

/**
 * Created by peter on 06.01.18.
 */
public class DocumentGuardBucketName extends BaseTypeString {
    public static final String BUCKET_SEPARATOR = "@DGL@";

    public DocumentGuardBucketName(String value) {
        super(value);
    }
}
