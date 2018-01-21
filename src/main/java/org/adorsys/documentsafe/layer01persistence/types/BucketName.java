package org.adorsys.documentsafe.layer01persistence.types;

import org.adorsys.documentsafe.layer00common.basetypes.BaseTypeString;
import org.adorsys.documentsafe.layer01persistence.exceptions.InvalidBucketNameException;

/**
 * Created by peter on 16.01.18.
 */
public class BucketName extends BaseTypeString {
    public final static String BUCKET_SEPARATOR = "/";

    public BucketName() {}

    public BucketName(String value) {
        super(value);
        if (value.indexOf(BUCKET_SEPARATOR) != -1) {
            throw new InvalidBucketNameException("BucketName " + value + " must not contain " + BUCKET_SEPARATOR);
        }
    }
}
