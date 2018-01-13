package org.adorsys.documentsafe.layer03rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;

/**
 * Created by peter on 10.01.18.
 */

@SpringBootApplication
/**
 * Die EnableAutoConfiguration(exclude jackson) Annotion ist sehr wichtig, denn sonst ziehen die TypeAdapter für Json nicht.
 * Dann erscheint
 * "documentKeyID": {value": "123"} statt
 * "documentKeyID": "123"
 */
@EnableAutoConfiguration(exclude = { JacksonAutoConfiguration.class })
public class RestApplication {
    private final static Logger LOGGER = LoggerFactory.getLogger(RestApplication.class);

    public static void main(String[] args) {
        LOGGER.info("START REST");
        SpringApplication.run(RestApplication.class, args);
    }
}