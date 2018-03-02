package org.adorsys.docusafe.rest.impl;

import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.WebRequest;

/**
 * Created by peter on 23.01.18 at 15:04.
 */
public enum JwtTokenExtractor {
    INSTANCE;

    private final static Logger LOGGER = LoggerFactory.getLogger(JwtTokenExtractor.class);

    public UserIDAuth getUserIDAuth(WebRequest request) {
        UserID userID = null;
        ReadKeyPassword readKeyPassword = null;
        try {
            userID = new UserID(request.getHeader("userid"));
            readKeyPassword = new ReadKeyPassword(request.getHeader("password"));
        } catch (Exception e) {
            return null;
        }
        return new UserIDAuth(userID, readKeyPassword);
    }

}
