package org.adorsys.documentsafe.layer04rest;

import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer04rest.types.VersionInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
            consumes = {MediaType.APPLICATION_JSON},
            produces = {MediaType.APPLICATION_JSON}
    )
    public VersionInformation getInfo() {
        LOGGER.debug("get Info");
        return new VersionInformation("affe", new DocumentKeyID("123"));
    }

    @RequestMapping(
            value = "/put",
            method = {RequestMethod.PUT},
            consumes = {MediaType.APPLICATION_JSON},
            produces = {MediaType.APPLICATION_JSON}
    )
    public void showDocumentBucketPath(@RequestBody VersionInformation versionInformation) {
        LOGGER.info("got a " + versionInformation);
    }
}
