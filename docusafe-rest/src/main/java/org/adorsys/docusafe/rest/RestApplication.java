package org.adorsys.docusafe.rest;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.miniostoreconnection.MinioParamParser;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;

import java.lang.reflect.Field;
import java.security.Security;
import java.util.Arrays;

/**
 * Created by peter on 10.01.18.
 */

@SpringBootApplication
/**
 * Die EnableAutoConfiguration(exclude jackson) Annotion ist sehr wichtig, denn sonst
 * ziehen die TypeAdapter für Json nicht.
 * Dann erscheint
 * "documentKeyID": {value": "123"} statt
 * "documentKeyID": "123"
 */
@EnableAutoConfiguration(exclude = {JacksonAutoConfiguration.class})
public class RestApplication {
    private final static Logger LOGGER = LoggerFactory.getLogger(RestApplication.class);
    public static final String MONGO_ARG_PREFIX = "-DSC-MONGO";
    public static final String MINIO_ARG_PREFIX = "-DSC-MINIO=";
    public static final String FILESYSTEM_ARG_PREFIX = "-DSC-FILESYSTEM";

    public static void main(String[] args) {
        Arrays.stream(args).forEach(arg -> {
            LOGGER.info("Application runtime argument:" + arg);
                    if (arg.equalsIgnoreCase("-TurnOffEncPolicy") || arg.equalsIgnoreCase("-EncOff")) {
                        try {
                            Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
                            field.setAccessible(true);
                            field.set(null, Boolean.FALSE);
                            LOGGER.warn("************************************************");
                            LOGGER.warn("*                                              *");
                            LOGGER.warn("*  ******************************************  *");
                            LOGGER.warn("*  *                                        *  *");
                            LOGGER.warn("*  *  JAVA ENCRYPTION POLICY SWITCHED OFF   *  *");
                            LOGGER.warn("*  *                                        *  *");
                            LOGGER.warn("*  ******************************************  *");
                            LOGGER.warn("*                                              *");
                            LOGGER.warn("************************************************");
                        } catch (Exception e) {
                            throw BaseExceptionHandler.handle(e);
                        }
                    } else if (arg.startsWith(MONGO_ARG_PREFIX)) {
                        LOGGER.info("*************************************");
                        LOGGER.info("*                                   *");
                        LOGGER.info("*  USE MONGO DB                     *");
                        LOGGER.info("*  (mongo db has be started first)  *");
                        LOGGER.info("*                                   *");
                        LOGGER.info("*************************************");
                        DocumentSafeController.storeConnection = DocumentSafeController.STORE_CONNECTION.MONGO;
                    } else if (arg.startsWith(MINIO_ARG_PREFIX)) {
                        String minioParams = arg.substring(MINIO_ARG_PREFIX.length());
                        MinioParamParser minioParamParser = new MinioParamParser(minioParams);
                        LOGGER.info("***********************");
                        LOGGER.info("*                     *");
                        LOGGER.info("*  USE MINIO SYSTEM   *");
                        LOGGER.info("*                     *");
                        LOGGER.info("***********************");
                        DocumentSafeController.storeConnection = DocumentSafeController.STORE_CONNECTION.MINIO;
                        DocumentSafeController.minioParams = minioParamParser;
                    } else if (arg.startsWith(FILESYSTEM_ARG_PREFIX)) {
                        LOGGER.info("**********************");
                        LOGGER.info("*                    *");
                        LOGGER.info("*  USE FILE SYSTEM   *");
                        LOGGER.info("*                    *");
                        LOGGER.info("**********************");
                        DocumentSafeController.storeConnection = DocumentSafeController.STORE_CONNECTION.FILESYSTEM;
                    } else {
                        LOGGER.error("Parameter " + arg + " is unknown.");
                        LOGGER.error("Knwon Parameters are: encoff, mongodb, filesystem");
                        throw new BaseException("Parameter " + arg + " is unknown.");
                    }
                }
        );
        LOGGER.info("add bouncy castle provider");
        Security.addProvider(new

                BouncyCastleProvider());
        SpringApplication.run(RestApplication.class, args);
    }
}