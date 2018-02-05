package org.adorsys.documentsafe.layer04rest.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.documentsafe.layer02service.types.ReadKeyPassword;
import org.adorsys.documentsafe.layer03business.types.UserID;
import org.adorsys.documentsafe.layer03business.types.complex.UserIDAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.WebRequest;

import javax.jws.WebResult;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.StringTokenizer;

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
