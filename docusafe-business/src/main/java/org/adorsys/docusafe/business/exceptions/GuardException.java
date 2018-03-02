package org.adorsys.docusafe.business.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

/**
 * Created by peter on 19.01.18 at 17:40.
 */
public class GuardException extends BaseException {
    public GuardException(String message) {
        super(message);
    }
}
