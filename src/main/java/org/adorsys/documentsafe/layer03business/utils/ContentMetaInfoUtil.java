package org.adorsys.documentsafe.layer03business.utils;

import org.adorsys.documentsafe.layer03business.types.complex.DSDocument;
import org.adorsys.documentsafe.layer03business.types.complex.DSDocumentMetaInfo;
import org.adorsys.documentsafe.layer03business.types.complex.DocumentLinkAsDSDocument;
import org.adorsys.encobject.domain.ContentMetaInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by peter on 23.01.18 at 18:01.
 */
public class ContentMetaInfoUtil {
    private final static String SIZE = "size";
    private final static String LINK = "link";
    public static ContentMetaInfo createContentMetaInfo(DSDocument dsDocument) {
        ContentMetaInfo contentMetaInfo = new ContentMetaInfo();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(SIZE, new Long(dsDocument.getDocumentContent().getValue().length));

        if (dsDocument instanceof DocumentLinkAsDSDocument) {
            map.put(LINK, Boolean.TRUE);
        }

        contentMetaInfo.setAddInfos(map);
        return contentMetaInfo;
    }

    public static DSDocumentMetaInfo createDSDocumentMetaInfo(ContentMetaInfo contentMetaInfo) {
        Map<String, Object> addInfos = contentMetaInfo.getAddInfos();
        Long size = (Long) addInfos.get(SIZE);
        return new DSDocumentMetaInfo(size);
    }

    public static boolean isLink(ContentMetaInfo contentMetaInfo) {
        Map<String, Object> addInfos = contentMetaInfo.getAddInfos();
        return addInfos.get(LINK) != null;
    }


    public static String show(ContentMetaInfo contentMetaInfo) {
        if (contentMetaInfo == null) {
            return "ContentMetaImfo is null";
        }

        Map<String, Object> addInfos = contentMetaInfo.getAddInfos();
        if (addInfos == null) {
            return "contentMetaInfo contains no metaInfo";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ContentMetaInfo ");
        for (String key : addInfos.keySet()) {
            Object value = addInfos.get(key);
            sb.append("(key:" + key + " value:" + value + ")");
        }
        return sb.toString();
    }
}
