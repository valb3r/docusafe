package org.adorsys.docusafe.spring.config;

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
            "      database: (mandatory)\n" +
            "      host: (mandatory)\n" +
            "      port: (mandatory)\n" +
            "      user: (optional)\n" +
            "      password: (optional)\n" +
            SpringConnectionPropertiesImpl.template;

    private String database;
    private String host;
    private String port;
    @Nullable
    private String user = null;
    @Nullable
    private String password = null;

    @Override
    public MongoDatabaseName getMongoDatabaseName() {
        return new MongoDatabaseName(database);
    }

    @Override
    public MongoHost getMongoHost() {
        return new MongoHost(host);
    }

    @Override
    public MongoPort getMongoPort() {
        return new MongoPort(Long.parseLong(port));
    }

    @Override
    public MongoUser getMongoUser() {
        if (user == null) {
            return null;
        }
        return new MongoUser(user);
    }

    @Override
    public MongoPassword getMongoPassword() {
        if (password == null) {
            return null;
        }
        return new MongoPassword(password);
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
