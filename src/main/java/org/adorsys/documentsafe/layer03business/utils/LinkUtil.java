package org.adorsys.documentsafe.layer03business.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.adorsys.documentsafe.layer02service.types.DocumentContent;
import org.adorsys.documentsafe.layer03business.types.complex.DSDocument;
import org.adorsys.documentsafe.layer03business.types.complex.DocumentFQN;
import org.adorsys.documentsafe.layer03business.types.complex.DocumentLink;
import org.adorsys.documentsafe.layer03business.types.complex.DocumentLinkAsDSDocument;

/**
 * Created by peter on 23.01.18 at 17:27.
 */
public class LinkUtil {
    public static DocumentLinkAsDSDocument createDSDocument(DocumentLink documentLink, DocumentFQN documentFQN) {
        Gson gson = new GsonBuilder().create();
        String jsonString = gson.toJson(documentLink);
        DocumentContent documentContent = new DocumentContent(jsonString.getBytes());
        return new DocumentLinkAsDSDocument(documentFQN, documentContent);
    }

    public static DocumentLink getDocumentLink(DocumentContent documentContent) {
        Gson gson = new GsonBuilder().create();
        String jsonString = new String(documentContent.getValue());
        DocumentLink documentLink = gson.fromJson(jsonString, DocumentLink.class);
        return documentLink;
    }
}
