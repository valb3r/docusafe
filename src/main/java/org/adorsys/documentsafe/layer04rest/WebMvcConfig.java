package org.adorsys.documentsafe.layer04rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.adorsys.documentsafe.layer01persistence.types.BucketName;
import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer02service.types.ReadKeyPassword;
import org.adorsys.documentsafe.layer03business.types.AccessType;
import org.adorsys.documentsafe.layer03business.types.UserID;
import org.adorsys.documentsafe.layer03business.types.complex.DocumentDirectoryFQN;
import org.adorsys.documentsafe.layer03business.types.complex.DocumentFQN;
import org.adorsys.documentsafe.layer04rest.adapter.AccessTypeJsonAdapter;
import org.adorsys.documentsafe.layer04rest.adapter.BucketNameJsonAdapter;
import org.adorsys.documentsafe.layer04rest.adapter.DocumentDirectoryFQNJsonAdapter;
import org.adorsys.documentsafe.layer04rest.adapter.DocumentFQNJsonAdapter;
import org.adorsys.documentsafe.layer04rest.adapter.DocumentKeyIDJsonAdapter;
import org.adorsys.documentsafe.layer04rest.adapter.ReadKeyPasswordJsonAdapter;
import org.adorsys.documentsafe.layer04rest.adapter.UserIDJsonAdapter;
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
                .create();

        GsonHttpMessageConverter gsonConverter = new GsonHttpMessageConverter();
        gsonConverter.setGson(gson);

        return gsonConverter;
    }

}