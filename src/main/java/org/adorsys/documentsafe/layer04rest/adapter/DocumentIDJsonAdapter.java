package org.adorsys.documentsafe.layer04rest.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.adorsys.documentsafe.layer02service.types.DocumentID;

import java.io.IOException;

/**
 * Created by peter on 13.01.18.
 */
public class DocumentIDJsonAdapter extends TypeAdapter<DocumentID> {
    @Override
    public void write(JsonWriter out, DocumentID documentID) throws IOException {
        out.value(documentID.getValue());
    }
    @Override
    public DocumentID read(JsonReader in) throws IOException {
        return new DocumentID(in.nextString());
    }
}