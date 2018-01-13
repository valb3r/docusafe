package org.adorsys.documentsafe.layer02service.types;

import com.google.gson.annotations.JsonAdapter;
import org.adorsys.documentsafe.layer00common.basetypes.BaseTypeString;
import org.adorsys.documentsafe.layer03rest.adapter.DocumentBucketNameJsonAdapter;
import org.adorsys.documentsafe.layer03rest.adapter.DocumentBucketNameXmlAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by peter on 06.01.18.
 */
@XmlJavaTypeAdapter(DocumentBucketNameXmlAdapter.class)
@JsonAdapter(DocumentBucketNameJsonAdapter.class)
public class DocumentBucketName extends BaseTypeString {
    // public static final String BUCKET_SEPARATOR = "@";

    public DocumentBucketName(String value) {
        super(value);
    }

}
