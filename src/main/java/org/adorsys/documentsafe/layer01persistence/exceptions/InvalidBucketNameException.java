package org.adorsys.documentsafe.layer01persistence.exceptions;

import org.adorsys.documentsafe.layer00common.exceptions.BaseException;

/**
 * Created by peter on 16.01.18.
 */
public class InvalidBucketNameException extends BaseException {
    public InvalidBucketNameException(String message) {
        super(message);
    }
}
