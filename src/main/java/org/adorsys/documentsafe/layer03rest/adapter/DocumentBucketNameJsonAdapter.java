package org.adorsys.documentsafe.layer03rest.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.adorsys.documentsafe.layer02service.types.DocumentBucketName;

import java.io.IOException;

/**
 * Created by peter on 13.01.18.
 */
public class DocumentBucketNameJsonAdapter extends TypeAdapter<DocumentBucketName> {
    @Override
    public void write(JsonWriter out, DocumentBucketName documentBucketName) throws IOException {
        out.value(documentBucketName.getValue());
    }
    @Override
    public DocumentBucketName read(JsonReader in) throws IOException {
        return new DocumentBucketName(in.nextString());
    }
}