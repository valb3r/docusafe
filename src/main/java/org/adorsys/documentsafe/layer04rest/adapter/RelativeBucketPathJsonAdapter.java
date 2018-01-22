package org.adorsys.documentsafe.layer04rest.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.adorsys.documentsafe.layer03business.types.RelativeBucketPath;

import java.io.IOException;

/**
 * Created by peter on 22.01.2018 at 20:43:35.
 */
public class RelativeBucketPathJsonAdapter extends TypeAdapter<RelativeBucketPath> {
    @Override
    public void write(JsonWriter out, RelativeBucketPath value) throws IOException {
        out.value(value.getObjectHandlePath());
    }
    @Override
    public RelativeBucketPath read(JsonReader in) throws IOException {
        return new RelativeBucketPath(in.nextString());
    }
}
