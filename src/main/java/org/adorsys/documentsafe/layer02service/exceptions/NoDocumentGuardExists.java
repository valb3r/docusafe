package org.adorsys.documentsafe.layer02service.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.encobject.complextypes.BucketPath;

/**
 * Created by peter on 03.02.18 at 09:52.
 */
public class NoDocumentGuardExists extends BaseException {
    private final BucketPath guardBucketPath;

    public NoDocumentGuardExists(BucketPath guardBucketPath) {
        super(getMessage(guardBucketPath));
        this.guardBucketPath = guardBucketPath;
    }

    private static String getMessage(BucketPath guardBucketPath) {
        return guardBucketPath.toString();
    }
}
