package org.adorsys.documentsafe.layer04rest.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentBucketPath;

import java.io.IOException;

/**
 * Created by peter on 21.01.18 at 11:42.
 */
public class DocumentBucketPathJsonAdapter extends TypeAdapter<DocumentBucketPath> {
    @Override
    public void write(JsonWriter out, DocumentBucketPath bucketPath) throws IOException {
        out.value(bucketPath.getObjectHandlePath());
    }
    @Override
    public DocumentBucketPath read(JsonReader in) throws IOException {
        return new DocumentBucketPath(in.nextString());
    }
}
