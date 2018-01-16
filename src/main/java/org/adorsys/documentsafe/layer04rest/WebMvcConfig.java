package org.adorsys.documentsafe.layer04rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.adorsys.documentsafe.layer01persistence.types.BucketName;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentBucketPath;
import org.adorsys.documentsafe.layer02service.types.DocumentID;
import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentLocation;
import org.adorsys.documentsafe.layer04rest.adapter.BucketNameJsonAdapter;
import org.adorsys.documentsafe.layer04rest.adapter.DocumentIDJsonAdapter;
import org.adorsys.documentsafe.layer04rest.adapter.DocumentKeyIDJsonAdapter;
import org.adorsys.documentsafe.layer04rest.types.VersionInformation;
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
                .registerTypeAdapter(BucketName.class, new BucketNameJsonAdapter())
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

    private Jaxb2Marshaller jaxbMarshaller() {
        Map<String, Object> props = new HashMap<>();
        props.put(Marshaller.JAXB_ENCODING, "UTF-8");
        props.put(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        Jaxb2Marshaller jaxbMarshaller = new Jaxb2Marshaller();
        jaxbMarshaller.setMarshallerProperties(props);
        jaxbMarshaller.setClassesToBeBound(
                VersionInformation.class,
                DocumentBucketPath.class,
                DocumentLocation.class,
                BucketName.class,
                DocumentKeyID.class);
        /**
         * jaxbMarshaller.setAdapters funktioniert nicht bzw. ist nur für
         * Adapter gedacht, die einen Parameter im Konstruktor haben.
         * Dann muss die Annotation benutzt werden UND hier die Methode
         * setAdapter
         */

        return jaxbMarshaller;
    }

}