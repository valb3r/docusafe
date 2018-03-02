package org.adorsys.docusafe.rest.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.adorsys.docusafe.business.types.UserID;

/**
 * Created by peter on 22.01.2018 at 20:05:52.
 */
public class UserIDJsonAdapter extends TypeAdapter<UserID> {
    @Override
    public void write(JsonWriter out, UserID value) throws IOException {
        out.value(value.getValue());
    }
    @Override
    public UserID read(JsonReader in) throws IOException {
        return new UserID(in.nextString());
    }
}
