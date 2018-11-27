package org.adorsys.docusafe.business.types.complex;

import org.adorsys.encobject.domain.UserMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 23.01.18 at 18:47.
 */
public class DSDocumentMetaInfo extends UserMetaData {
    private final static Logger LOGGER = LoggerFactory.getLogger(DSDocumentMetaInfo.class);
    public DSDocumentMetaInfo() {

    }

    public DSDocumentMetaInfo(UserMetaData otherUserMetadata) {
        if (otherUserMetadata != null) {
            for (String key : otherUserMetadata.keySet()) {
                put(key, otherUserMetadata.get(key));
            }
        }
    }


}
