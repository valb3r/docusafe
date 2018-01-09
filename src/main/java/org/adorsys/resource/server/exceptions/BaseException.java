package org.adorsys.resource.server.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 23.12.17 at 18:07.
 */
public class BaseException extends RuntimeException {
        private final static Logger log = LoggerFactory.getLogger(BaseException.class);
        public BaseException(String message, Throwable cause) {
            super(message, cause);
//            log.error(BaseExceptionHandler.ThrowableToString(0, this, log.isDebugEnabled()));
            log.error(BaseExceptionHandler.ThrowableToString(0, this, true));
        }
        public BaseException(Throwable cause) {
            super(cause);
//            log.error(BaseExceptionHandler.ThrowableToString(0, this, log.isDebugEnabled()));
            log.error(BaseExceptionHandler.ThrowableToString(0, this, true));
        }
        public BaseException(String message) {
            super(message);
//            log.error(BaseExceptionHandler.ThrowableToString(0, this, log.isDebugEnabled()));
            log.error(BaseExceptionHandler.ThrowableToString(0, this, true));
        }
    }