package org.adorsys.docusafe.rest.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.service.exceptions.NoDocumentGuardExists;
import org.adorsys.docusafe.business.exceptions.NoWriteAccessException;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.rest.impl.JwtTokenExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Created by peter on 05.02.18 at 12:10.
 * see https://restpatterns.mindtouch.us/HTTP_Status_Codes
 *
 */
@ControllerAdvice
public class SpringRestExceptionHandler  extends ResponseEntityExceptionHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(SpringRestExceptionHandler.class);

    @ExceptionHandler(value = { NoWriteAccessException.class })
    protected ResponseEntity<Object> handleConflict(NoWriteAccessException ex, WebRequest request) {
        return handleExceptionInternal(ex, new RestError(ex.getMessage()),
                new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(value = { NoDocumentGuardExists.class })
    protected ResponseEntity<Object> handleConflict(NoDocumentGuardExists ex, WebRequest request) {
        UserIDAuth userIDAuth = JwtTokenExtractor.INSTANCE.getUserIDAuth(request);
        if (userIDAuth != null) {
            return handleExceptionInternal(ex, new RestError("User " + userIDAuth.getUserID().getValue() + " has no access right to read this resource"),
                    new HttpHeaders(), HttpStatus.FORBIDDEN, request);

        } else {
            return handleExceptionInternal(ex, new RestError(ex.getMessage()),
                    new HttpHeaders(), HttpStatus.FORBIDDEN, request);
        }
    }

    @ExceptionHandler(value = { BaseException.class })
    protected ResponseEntity<Object> handleConflict(BaseException ex, WebRequest request) {
        return handleExceptionInternal(ex, new RestError(ex.getClass().getSimpleName() + " " + ex.getMessage()),
                new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(value = { Exception.class })
    protected ResponseEntity<Object> handleConflict(Exception ex, WebRequest request) {
        BaseException e = new BaseException("CATCHED IN SpringRestExceptionHandler", ex);
        return handleExceptionInternal(ex, new RestError(ex.getClass().getSimpleName() + " " + e.getMessage()),
                new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

}
