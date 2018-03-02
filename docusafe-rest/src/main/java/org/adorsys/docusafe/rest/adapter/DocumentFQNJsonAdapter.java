package org.adorsys.docusafe.rest.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;

import java.io.IOException;

/**
 * Created by peter on 24.01.2018 at 11:19:56.
 */
public class DocumentFQNJsonAdapter extends TypeAdapter<DocumentFQN> {
    @Override
    public void write(JsonWriter out, DocumentFQN value) throws IOException {
        out.value(value.getValue());
    }
    @Override
    public DocumentFQN read(JsonReader in) throws IOException {
        return new DocumentFQN(in.nextString());
    }
}
