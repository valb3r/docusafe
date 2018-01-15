package org.adorsys.documentsafe.layer03rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.adorsys.documentsafe.layer02service.types.DocumentBucketName;
import org.adorsys.documentsafe.layer02service.types.DocumentID;
import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentLocation;
import org.adorsys.documentsafe.layer03rest.adapter.DocumentBucketNameJsonAdapter;
import org.adorsys.documentsafe.layer03rest.adapter.DocumentBucketNameXmlAdapter;
import org.adorsys.documentsafe.layer03rest.adapter.DocumentIDJsonAdapter;
import org.adorsys.documentsafe.layer03rest.adapter.DocumentIDXmlAdapter;
import org.adorsys.documentsafe.layer03rest.adapter.DocumentKeyIDJsonAdapter;
import org.adorsys.documentsafe.layer03rest.adapter.DocumentKeyIDXmlAdapter;
import org.adorsys.documentsafe.layer03rest.types.VersionInformation;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.xml.bind.Marshaller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by peter on 13.01.18 at 22:28.
 */
@Configuration
@EnableWebMvc
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(createGsonHttpMessageConverter());
        converters.add(createXmlHttpMessageConverter());
        super.configureMessageConverters(converters);
    }

    private GsonHttpMessageConverter createGsonHttpMessageConverter() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(DocumentBucketName.class, new DocumentBucketNameJsonAdapter())
                .registerTypeAdapter(DocumentID.class, new DocumentIDJsonAdapter())
                .registerTypeAdapter(DocumentKeyID.class, new DocumentKeyIDJsonAdapter())
                .create();

        GsonHttpMessageConverter gsonConverter = new GsonHttpMessageConverter();
        gsonConverter.setGson(gson);

        return gsonConverter;
    }

    private HttpMessageConverter<Object> createXmlHttpMessageConverter() {
        MarshallingHttpMessageConverter xmlConverter = new MarshallingHttpMessageConverter();
        xmlConverter.setMarshaller(jaxbMarshaller());
        xmlConverter.setUnmarshaller(jaxbMarshaller());
        return xmlConverter;
    }

    public Jaxb2Marshaller jaxbMarshaller() {

        Jaxb2Marshaller jaxbMarshaller = new Jaxb2Marshaller();

        jaxbMarshaller.setAdapters(
                new DocumentKeyIDXmlAdapter(),
                new DocumentIDXmlAdapter(),
                new DocumentBucketNameXmlAdapter());

        Map<String, Object> props = new HashMap<>();
        props.put(Marshaller.JAXB_ENCODING, "UTF-8");
        props.put(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.setMarshallerProperties(props);
        jaxbMarshaller.setClassesToBeBound(
                VersionInformation.class,
                DocumentBucketName.class,
                DocumentLocation.class,
                DocumentKeyID.class);

        return jaxbMarshaller;

    }

}