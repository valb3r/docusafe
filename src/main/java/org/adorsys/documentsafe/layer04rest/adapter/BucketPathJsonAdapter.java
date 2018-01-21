package org.adorsys.documentsafe.layer04rest.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;

import java.io.IOException;

/**
 * Created by peter on 21.01.18 at 10:54.
 */
public class BucketPathJsonAdapter extends TypeAdapter<BucketPath> {
    @Override
    public void write(JsonWriter out, BucketPath bucketPath) throws IOException {
        out.value(bucketPath.getObjectHandlePath());
    }
    @Override
    public BucketPath read(JsonReader in) throws IOException {
        return new BucketPath(in.nextString());
    }
}
