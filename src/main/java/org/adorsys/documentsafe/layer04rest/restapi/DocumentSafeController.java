package org.adorsys.documentsafe.layer04rest.restapi;

import org.adorsys.documentsafe.layer03business.DocumentSafeService;
import org.adorsys.documentsafe.layer03business.impl.DocumentSafeServiceImpl;
import org.adorsys.documentsafe.layer03business.types.complex.DocumentFQN;
import org.adorsys.documentsafe.layer03business.types.complex.DSDocument;
import org.adorsys.documentsafe.layer03business.types.complex.UserIDAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;

/**
 * Created by peter on 22.01.18 at 19:27.
 * UserIDAuth wird natürlich kein expliziter Parameter sein. Aber die JWT Logik kommt
 * erst im zweiten Schritt. Jetzt erst mal loslegen mit explizitem Parameter.
 */
@RestController
public class DocumentSafeController {

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

    public void storeDocument(UserIDAuth userIDAuth, DSDocument dsDocument) {

    }

    public void destroyUser(UserIDAuth userIDAuth) {

    }

    @RequestMapping(
            value = "document",
            method = {RequestMethod.POST},
            consumes = {MediaType.APPLICATION_JSON},
            produces = {MediaType.APPLICATION_JSON}
    )
    public
    @ResponseBody
    DSDocument readDocument(@RequestBody UserIDAuth userIDAuth,
                            @RequestParam DocumentFQN documentFQN) {
        LOGGER.debug("recieved:" + userIDAuth + " and " + documentFQN);
        return service.readDocument(userIDAuth, documentFQN);
    }
}
