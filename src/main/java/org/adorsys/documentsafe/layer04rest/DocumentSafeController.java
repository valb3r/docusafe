package org.adorsys.documentsafe.layer04rest;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer03business.DocumentSafeService;
import org.adorsys.documentsafe.layer03business.impl.DocumentSafeServiceImpl;
import org.adorsys.documentsafe.layer03business.types.UserID;
import org.adorsys.documentsafe.layer03business.types.complex.DSDocument;
import org.adorsys.documentsafe.layer03business.types.complex.DocumentFQN;
import org.adorsys.documentsafe.layer03business.types.complex.UserIDAuth;
import org.adorsys.documentsafe.layer04rest.types.CreateLinkTupel;
import org.adorsys.documentsafe.layer04rest.types.GrantDocument;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.adorsys.encobject.filesystem.FileSystemExtendedStorageConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

/**
 * Created by peter on 22.01.18 at 19:27.
 * UserIDAuth wird natürlich kein expliziter Parameter sein. Aber die JWT Logik kommt
 * erst im zweiten Schritt. Jetzt erst mal loslegen mit explizitem Parameter.
 */
@RestController
public class DocumentSafeController {
    private final static String APPLICATION_JSON = "application/json";
    private final static String APPLICATION_OCTET_STREAM = "application/octet-stream";

    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentSafeController.class);
    private DocumentSafeService service = new DocumentSafeServiceImpl(new FileSystemExtendedStorageConnection());

    /**
     * USER
     * ===========================================================================================
     */
    @RequestMapping(
            value = "/internal/user",
            method = {RequestMethod.PUT},
            consumes = {APPLICATION_JSON},
            produces = {APPLICATION_JSON}
    )
    public void createUser(@RequestBody UserIDAuth userIDAuth) {
        service.createUser(userIDAuth);
    }

    @RequestMapping(
            value = "/internal/user",
            method = {RequestMethod.DELETE},
            consumes = {APPLICATION_JSON},
            produces = {APPLICATION_JSON}
    )
    public void destroyUser(@RequestBody UserIDAuth userIDAuth) {
        service.destroyUser(userIDAuth);
    }


    /**
     * DOCUMENT
     * ===========================================================================================
     */
    @RequestMapping(
            value = "/document",
            method = {RequestMethod.PUT},
            consumes = {APPLICATION_JSON}
    )
    public void storeDocument(@RequestHeader("userid") String userid,
                              @RequestHeader("password") String password,
                              @RequestBody DSDocument dsDocument) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(userid), new ReadKeyPassword(password));
        service.storeDocument(userIDAuth, dsDocument);
    }

    @RequestMapping(
            value = "/document/stream1",
            method = {RequestMethod.PUT},
            consumes = {APPLICATION_OCTET_STREAM}
    )
    public void storeDocument(@RequestHeader("userid") String userid,
                              @RequestHeader("password") String password,
                              InputStream inputStream) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(userid), new ReadKeyPassword(password));
        LOGGER.info("input auf document/stream1 for " + userIDAuth);
        show(inputStream);
    }

    @RequestMapping(
            value = "/document/**",
            method = {RequestMethod.GET},
            consumes = {APPLICATION_JSON},
            produces = {APPLICATION_JSON}
    )
    public
    @ResponseBody
    DSDocument readDocument(@RequestHeader("userid") String userid,
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


    /**
     * GRANT/DOCUMENT
     * ===========================================================================================
     */
    @RequestMapping(
            value = "/grant/document",
            method = {RequestMethod.PUT},
            consumes = {APPLICATION_JSON},
            produces = {APPLICATION_JSON}
    )
    public void grantAccess(@RequestHeader("userid") String userid,
                            @RequestHeader("password") String password,
                            @RequestBody GrantDocument grantDocument) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(userid), new ReadKeyPassword(password));
        service.grantAccessToUserForFolder(userIDAuth, grantDocument.getReceivingUser(), grantDocument.getDocumentDirectoryFQN(), grantDocument.getAccessType());
    }

    @RequestMapping(
            value = "/granted/document/{ownerUserID}/**",
            method = {RequestMethod.GET},
            consumes = {APPLICATION_JSON},
            produces = {APPLICATION_JSON}
    )
    public
    @ResponseBody
    DSDocument readGrantedDocument(@RequestHeader("userid") String userid,
                                   @RequestHeader("password") String password,
                                   @PathVariable("ownerUserID") String ownerUserIDString,
                                   HttpServletRequest request
    ) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(userid), new ReadKeyPassword(password));
        UserID ownerUserID = new UserID(ownerUserIDString);

        final String path = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        final String bestMatchingPattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();
        final String documentFQNStringWithQuotes = new AntPathMatcher().extractPathWithinPattern(bestMatchingPattern, path);
        final String documentFQNString = documentFQNStringWithQuotes.replaceAll("\"", "");

        DocumentFQN documentFQN = new DocumentFQN(documentFQNString);
        LOGGER.debug("received:" + userIDAuth + " and " + ownerUserID + " and " + documentFQN);
        return service.readDocument(userIDAuth, ownerUserID, documentFQN);
    }

    @RequestMapping(
            value = "/granted/document/{ownerUserID}",
            method = {RequestMethod.PUT},
            consumes = {APPLICATION_JSON},
            produces = {APPLICATION_JSON}
    )
    public void storeGrantedDocument(@RequestHeader("userid") String userid,
                                     @RequestHeader("password") String password,
                                     @PathVariable("ownerUserID") String ownerUserIDString,
                                     @RequestBody DSDocument dsDocument) {
        UserID ownerUserID = new UserID(ownerUserIDString);
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(userid), new ReadKeyPassword(password));
        service.storeDocument(userIDAuth, ownerUserID, dsDocument);
    }

    /**
     * DOCUMENT/LINK
     * ===========================================================================================
     */
    @RequestMapping(
            value = "/document/link",
            method = {RequestMethod.PUT},
            consumes = {APPLICATION_JSON},
            produces = {APPLICATION_JSON}
    )
    public void createLink(@RequestHeader("userid") String userid,
                           @RequestHeader("password") String password,
                           @RequestBody CreateLinkTupel createLinkTupel) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(userid), new ReadKeyPassword(password));
        service.linkDocument(userIDAuth, createLinkTupel.getSource(), createLinkTupel.getDestination());
    }

    private void show(InputStream inputStream) {
        try {
            LOGGER.info("ok, receive an inputstream");
            int available = 0;
            long sum = 0;
            boolean eof = false;
            while (!eof) {
                available = inputStream.available();
                if (available <= 1) {
                    // be blocked, until unexpected EOF Exception or Data availabel of expected EOF
                    int value = inputStream.read();
                    eof = value == -1;
                    if (! eof) {
                        byte[] bytes = new byte[1];
                        bytes[0] = (byte) value;
                        sum++;
                        LOGGER.info("READ 1 byte");
                    }
                } else {
                    byte[] bytes = new byte[available];
                    int read = inputStream.read(bytes, 0, available);
                    if (read != available) {
                        throw new BaseException("expected to read " + available + " bytes, but read " + read + " bytes");
                    }
                    sum += read;
                    LOGGER.info("READ " + available + " bytes");
                }
            }
            LOGGER.info("finished reading " + sum + " bytes");
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
