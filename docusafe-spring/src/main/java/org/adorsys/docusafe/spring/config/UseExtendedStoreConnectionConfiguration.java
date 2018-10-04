package org.adorsys.docusafe.spring.config;

import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.StringTokenizer;

/**
 * Created by peter on 02.10.18.
 */
@Configuration
@ComponentScan(basePackages = {
        "org.adorsys.docusafe.spring.config"
})
public class UseExtendedStoreConnectionConfiguration {
    private final static Logger LOGGER = LoggerFactory.getLogger(UseExtendedStoreConnectionConfiguration.class);

    @Bean
    public ExtendedStoreConnection extendedStoreConnection(UseDocusafeProperties properties) {
        String fulldescription = properties.getFulldescription();
        if (fulldescription != null) {
            LOGGER.debug("FOUND ******************************** parameter for storeconnection is " + fulldescription);
            StringTokenizer st = new StringTokenizer(fulldescription);
            int i = 0;
            String[] args = new String[st.countTokens()];
            while (st.hasMoreTokens()) {
                args[i++] = st.nextToken();
                LOGGER.debug("arg " + (i - 1) + " has value " + args[(i - 1)]);
            }
            String[] strings = ExtendedStoreConnectionFactory.readArguments(args);
            if (strings.length > 0) {
                for (String s : strings) {
                    LOGGER.error("parameter unknown: " + s);
                }
            }
        } else {
            LOGGER.debug("NOT FOUND ********************** extended store connection with default values");
        }
        LOGGER.debug("create extended store connection");
        return ExtendedStoreConnectionFactory.get();
    }
}
