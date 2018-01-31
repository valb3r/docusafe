package org.adorsys.documentsafe.layer04rest.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.documentsafe.layer02service.types.ReadKeyPassword;
import org.adorsys.documentsafe.layer03business.types.UserID;
import org.adorsys.documentsafe.layer03business.types.complex.UserIDAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public UserIDAuth getUserIDAuth(HttpHeaders headers) {
        MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
        UserID userID = null;
        ReadKeyPassword readKeyPassword = null;

        {
            List<String> elements = requestHeaders.get("userid");
            if (elements != null && !elements.isEmpty()) {
                for (int i = 0; i < elements.size(); i++) {
                    String token = elements.get(i);
                    LOGGER.info("Element " + i + " :" + token);
                    userID = new UserID(token);
                }
            }
        }
        {
            List<String> elements = requestHeaders.get("password");
            if (elements != null && !elements.isEmpty()) {
                for (int i = 0; i < elements.size(); i++) {
                    String token = elements.get(i);
                    LOGGER.info("Element " + i + " :" + token);
                    readKeyPassword = new ReadKeyPassword(token);
                }
            }
        }
        if (userID == null || readKeyPassword == null) {
            throw new BaseException("missing headerfields userid and password");
        }
        return new UserIDAuth(userID, readKeyPassword);

    }

}
