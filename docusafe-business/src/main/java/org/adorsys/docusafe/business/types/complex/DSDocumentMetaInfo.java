package org.adorsys.docusafe.business.types.complex;

import org.adorsys.docusafe.service.DocumentPersistenceService;
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

    public void setNoEncryption() {
        DocumentPersistenceService.setNotEncrypted(this);
    }

    public boolean isNotEncrypted() {
        if (System.getProperty("UGLY_UGLY_NO_ENCRYPTION_AT_ALL_NEVER_TO_BE_USED", "false").equalsIgnoreCase("true")) {
            LOGGER.warn("ENCRYPTION DISABLED BY SYSTEM PROPERTY");
            setNoEncryption();
            return true;
       }
        return DocumentPersistenceService.isNotEncrypted(this);
    }
}
