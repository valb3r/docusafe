package org.adorsys.docusafe.spring.config;

import org.adorsys.cryptoutils.mongodbstoreconnection.MongoParamParser;
import org.adorsys.encobject.types.connection.*;
import org.adorsys.encobject.types.properties.MongoConnectionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Created by peter on 05.10.18.
 */
@Component
@ConfigurationProperties(prefix = "docusafe.storeconnection.mongo")
@Validated
public class SpringMongoConnectionProperties extends SpringConnectionPropertiesImpl implements MongoConnectionProperties {
    private final static Logger LOGGER = LoggerFactory.getLogger(SpringMongoConnectionProperties.class);

    public final static String template = "\n" +
            "docusafe:\n" +
            "  storeconnection:\n" +
            "    mongo:\n" +
            "      mongoURI: (mandatory)\n" +
            "                (" + MongoParamParser.EXPECTED_PARAMS + ")" +
            SpringConnectionPropertiesImpl.template;

    private String mongoURI;

    @Override
    public MongoURI getMongoURI() {
        return new MongoURI(mongoURI);
    }

    public void setMongoURI(String mongoURI) {
        this.mongoURI = mongoURI;
    }
}
