package org.adorsys.docusafe.cached.transactional.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

/**
 * Created by peter on 21.06.18 at 12:43.
 */
public class CacheException extends BaseException {
    public CacheException(String message) {
        super(message);
    }

}
