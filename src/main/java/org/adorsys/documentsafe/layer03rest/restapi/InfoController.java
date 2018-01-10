package org.adorsys.documentsafe.layer03rest.restapi;

import org.adorsys.documentsafe.layer02service.types.DocumentKey;
import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer03rest.types.VersionInformation;
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
    @RequestMapping(
            value = "/info",
            method = {RequestMethod.GET},
            produces = {MediaType.APPLICATION_JSON}
    )
    public VersionInformation getInfo() {
        return new VersionInformation("affe", new DocumentKeyID("123"));
    }
}
