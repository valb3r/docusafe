package org.adorsys.docusafe.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import org.adorsys.docusafe.rest.adapter.SpringfoxJsonToGsonAdapter;
import org.adorsys.docusafe.rest.adapter.UserIDJsonAdapter;
import org.adorsys.docusafe.service.types.AccessType;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.docusafe.service.types.DocumentKeyID;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.adorsys.encobject.types.BucketName;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.spring.web.json.Json;

import java.util.List;

/**
 * Created by peter on 13.01.18 at 22:28.
 */
@SpringBootApplication(exclude = {JacksonAutoConfiguration.class, MongoAutoConfiguration.class})
@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(createGsonHttpMessageConverter());
        RestTemplate r = new RestTemplate();
        r.setMessageConverters(converters);
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
                .registerTypeAdapter(Json.class, new SpringfoxJsonToGsonAdapter())
                .create();

        GsonHttpMessageConverter gsonConverter = new GsonHttpMessageConverter();
        gsonConverter.setGson(gson);

        return gsonConverter;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

}
