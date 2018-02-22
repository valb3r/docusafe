package org.adorsys.documentsafe.layer03business.types.complex;

import org.adorsys.encobject.domain.StorageMetadata;

/**
 * Created by peter on 23.01.18 at 18:47.
 */
public class DSDocumentMetaInfo {
    private Long size;

    public DSDocumentMetaInfo(StorageMetadata storageMetadata) {
        this.size = storageMetadata.getSize();
    }

    public Long getSize() {
        return size;
    }

}
