package org.adorsys.documentsafe.layer03rest.restapi;

import org.adorsys.documentsafe.layer02service.types.DocumentKey;
import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer03rest.types.VersionInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import javax.ws.rs.core.MediaType;

/**
 * Created by peter on 10.01.18.
 */
@RestController
public class InfoController {
    private final static Logger LOGGER = LoggerFactory.getLogger(InfoController.class);
    @RequestMapping(
            value = "/info",
            method = {RequestMethod.GET},
            consumes = {MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON},
            produces = {MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON}
    )
    public VersionInformation getInfo() {
        LOGGER.info("et Info");
        return new VersionInformation("affe", new DocumentKeyID("123"));
    }
}
