package org.adorsys.documentsafe.layer04rest;

import org.adorsys.documentsafe.layer02service.types.ReadKeyPassword;
import org.adorsys.documentsafe.layer03business.DocumentSafeService;
import org.adorsys.documentsafe.layer03business.impl.DocumentSafeServiceImpl;
import org.adorsys.documentsafe.layer03business.types.UserID;
import org.adorsys.documentsafe.layer03business.types.complex.DocumentFQN;
import org.adorsys.documentsafe.layer03business.types.complex.DSDocument;
import org.adorsys.documentsafe.layer03business.types.complex.UserIDAuth;
import org.adorsys.documentsafe.layer04rest.impl.FileSystemBlobStoreFactory;
import org.adorsys.documentsafe.layer04rest.impl.JwtTokenExtractor;
import org.adorsys.documentsafe.layer04rest.types.CreateLinkTupel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/**
 * Created by peter on 22.01.18 at 19:27.
 * UserIDAuth wird natürlich kein expliziter Parameter sein. Aber die JWT Logik kommt
 * erst im zweiten Schritt. Jetzt erst mal loslegen mit explizitem Parameter.
 */
@RestController
public class DocumentSafeController {

    @Context
    private HttpHeaders headers;

    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentSafeController.class);
    private DocumentSafeService service = new DocumentSafeServiceImpl(new FileSystemBlobStoreFactory());

    @RequestMapping(
            value = "/internal/user",
            method = {RequestMethod.PUT},
            consumes = {MediaType.APPLICATION_JSON},
            produces = {MediaType.APPLICATION_JSON}
    )
    public void createUser(@RequestBody UserIDAuth userIDAuth) {
        service.createUser(userIDAuth);
    }

    @RequestMapping(
            value = "/document",
            method = {RequestMethod.PUT},
            consumes = {MediaType.APPLICATION_JSON},
            produces = {MediaType.APPLICATION_JSON}
    )
    public void storeDocument(@RequestHeader("userid") String userid,
                              @RequestHeader("password") String password,
                              @RequestBody DSDocument dsDocument) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(userid), new ReadKeyPassword(password));
        service.storeDocument(userIDAuth, dsDocument);
    }

    @RequestMapping(
            value = "/internal/user",
            method = {RequestMethod.DELETE},
            consumes = {MediaType.APPLICATION_JSON},
            produces = {MediaType.APPLICATION_JSON}
    )
    public void destroyUser(@RequestHeader("userid") String userid,
                            @RequestHeader("password") String password) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(userid), new ReadKeyPassword(password));
        service.destroyUser(userIDAuth);
    }

    @RequestMapping(
            value = "/document/**",
            method = {RequestMethod.GET},
            consumes = {MediaType.APPLICATION_JSON},
            produces = {MediaType.APPLICATION_JSON}
    )
    public @ResponseBody DSDocument readDocument(@RequestHeader("userid") String userid,
                                                 @RequestHeader("password") String password,
                                                 HttpServletRequest request
    ) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(userid), new ReadKeyPassword(password));

        final String path = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        final String bestMatchingPattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();
        final String documentFQNStringWithQuotes = new AntPathMatcher().extractPathWithinPattern(bestMatchingPattern, path);
        final String documentFQNString = documentFQNStringWithQuotes.replaceAll("\"", "");

        DocumentFQN documentFQN = new DocumentFQN(documentFQNString);
        LOGGER.debug("received:" + userIDAuth + " and " + documentFQN);
        return service.readDocument(userIDAuth, documentFQN);
    }

    @RequestMapping(
            value = "/document/link",
            method = {RequestMethod.PUT},
            consumes = {MediaType.APPLICATION_JSON},
            produces = {MediaType.APPLICATION_JSON}
    )
    public void createLink(@RequestHeader("userid") String userid,
                           @RequestHeader("password") String password,
                           @RequestBody CreateLinkTupel createLinkTupel) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(userid), new ReadKeyPassword(password));
        service.linkDocument(userIDAuth, createLinkTupel.getSource(), createLinkTupel.getDestination());
    }

}
