package org.adorsys.docusafe.rest;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.cryptoutils.storeconnectionfactory.ReadArguments;
import org.adorsys.cryptoutils.utils.ShowProperties;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

import java.lang.reflect.Field;
import java.security.Security;
import java.util.Arrays;

/**
 * Created by peter on 10.01.18.
 */

@SpringBootApplication(exclude = {JacksonAutoConfiguration.class, MongoAutoConfiguration.class})
/**
 * Die EnableAutoConfiguration(exclude jackson) Annotion ist sehr wichtig, denn sonst
 * ziehen die TypeAdapter für Json nicht.
 * Dann erscheint
 * "documentKeyID": {value": "123"} statt
 * "documentKeyID": "123"
 */
@EnableAutoConfiguration(exclude = {JacksonAutoConfiguration.class, MongoAutoConfiguration.class})
public class RestApplication {
    private final static Logger LOGGER = LoggerFactory.getLogger(RestApplication.class);

    public static void main(String[] origargs) {
        String[] args = ExtendedStoreConnectionFactory.readArguments(origargs);
        Arrays.stream(args).forEach(arg -> {
                    LOGGER.debug("Application runtime argument:" + arg);
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
                    } else if (arg.equalsIgnoreCase("-ERASE_DATABASE")) {
                        LOGGER.warn("***************************");
                        LOGGER.warn("*                         *");
                        LOGGER.warn("*  *********************  *");
                        LOGGER.warn("*  *                   *  *");
                        LOGGER.warn("*  *  DATABASE ERASED  *  *");
                        LOGGER.warn("*  *                   *  *");
                        LOGGER.warn("*  *********************  *");
                        LOGGER.warn("*                         *");
                        LOGGER.warn("***************************");
                        ExtendedStoreConnection extendedStoreConnection = ExtendedStoreConnectionFactory.get();
                        extendedStoreConnection.listAllBuckets().forEach(c -> extendedStoreConnection.deleteContainer(c));
                    } else {
                        LOGGER.error("Parameter " + arg + " is unknown.");
                        LOGGER.error("Knwon Parameters are: " +
                                "-TurnOffEncPolicy, " +
                                "-EncOff, " +
                                "-ERASE_DATABASE, " +
                                ReadArguments.FILESYSTEM_ARG + ", " +
                                ReadArguments.MINIO_ARG + ", " +
                                ReadArguments.MONGO_ARG + " ");
                        LOGGER.error("The StoreConnection to choose can be supplied via Systemproperties too :" +
                                ReadArguments.FILESYSTEM + ", " +
                                ReadArguments.MINIO+ ", " +
                                ReadArguments.MONGO + " ");

                        throw new BaseException("Parameter " + arg + " is unknown.");
                    }
                }
        );
        LOGGER.debug("add bouncy castle provider");
        Security.addProvider(new BouncyCastleProvider());
        ShowProperties.log();
        try {
            SpringApplication.run(RestApplication.class, args);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
