package org.adorsys.docusafe.rest.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.adorsys.cryptoutils.utils.HexUtil;
import org.adorsys.docusafe.service.types.DocumentContent;

import java.io.IOException;

/**
 * Created by peter on 27.02.2018 at 11:13:30.
 */
public class DocumentContentJsonAdapter extends TypeAdapter<DocumentContent> {
    @Override
    public void write(JsonWriter out, DocumentContent value) throws IOException {
        out.value(HexUtil.convertBytesToHexString(value.getValue()));
    }
    @Override
    public DocumentContent read(JsonReader in) throws IOException {
        return new DocumentContent(HexUtil.convertHexStringToBytes(in.nextString()));
    }
}
