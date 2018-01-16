package org.adorsys.documentsafe.layer01persistence.exceptions;

import org.adorsys.documentsafe.layer00common.exceptions.BaseException;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;

/**
 * Created by peter on 16.01.18.
 */
public class BucketException extends BaseException {
    public BucketException(String message) {
        super(message);
    }
}
