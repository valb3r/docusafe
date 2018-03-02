package org.adorsys.docusafe.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.adorsys.docusafe.service.types.AccessType;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.docusafe.service.types.DocumentKeyID;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.rest.adapter.AccessTypeJsonAdapter;
import org.adorsys.docusafe.rest.adapter.BucketNameJsonAdapter;
import org.adorsys.docusafe.rest.adapter.DocumentContentJsonAdapter;
import org.adorsys.docusafe.rest.adapter.DocumentDirectoryFQNJsonAdapter;
import org.adorsys.docusafe.rest.adapter.DocumentFQNJsonAdapter;
import org.adorsys.docusafe.rest.adapter.DocumentKeyIDJsonAdapter;
import org.adorsys.docusafe.rest.adapter.ReadKeyPasswordJsonAdapter;
import org.adorsys.docusafe.rest.adapter.UserIDJsonAdapter;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.adorsys.encobject.types.BucketName;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * Created by peter on 13.01.18 at 22:28.
 */
@Configuration
@EnableWebMvc
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(createGsonHttpMessageConverter());
        super.configureMessageConverters(converters);
    }

    private GsonHttpMessageConverter createGsonHttpMessageConverter() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(BucketName.class, new BucketNameJsonAdapter())
                .registerTypeAdapter(DocumentKeyID.class, new DocumentKeyIDJsonAdapter())
                .registerTypeAdapter(UserID.class, new UserIDJsonAdapter())
                .registerTypeAdapter(ReadKeyPassword.class, new ReadKeyPasswordJsonAdapter())
                .registerTypeAdapter(DocumentFQN.class, new DocumentFQNJsonAdapter())
                .registerTypeAdapter(AccessType.class, new AccessTypeJsonAdapter())
                .registerTypeAdapter(DocumentDirectoryFQN.class, new DocumentDirectoryFQNJsonAdapter())
                .registerTypeAdapter(DocumentContent.class, new DocumentContentJsonAdapter())
                .create();

        GsonHttpMessageConverter gsonConverter = new GsonHttpMessageConverter();
        gsonConverter.setGson(gson);

        return gsonConverter;
    }

}