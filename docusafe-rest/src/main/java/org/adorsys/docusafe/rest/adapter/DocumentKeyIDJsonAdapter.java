package org.adorsys.docusafe.rest.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.adorsys.docusafe.service.types.DocumentKeyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by peter on 13.01.18.
 */
public class DocumentKeyIDJsonAdapter extends TypeAdapter<DocumentKeyID> {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentKeyIDJsonAdapter.class);

    @Override
    public void write(JsonWriter out, DocumentKeyID documentKeyID) throws IOException {
        out.value(documentKeyID.getValue());
    }
    @Override
    public DocumentKeyID read(JsonReader in) throws IOException {
        return new DocumentKeyID(in.nextString());
    }
}
