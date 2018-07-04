package org.adorsys.docusafe.service;

import org.adorsys.docusafe.business.types.MemoryContext;
import org.adorsys.docusafe.service.types.complextypes.DocumentBucketPath;
import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.encobject.domain.KeyStoreAccess;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.UserMetaData;
import org.adorsys.encobject.types.OverwriteFlag;

/**
 * Created by peter on 11.01.18.
 */
public interface DocumentPersistenceService {


    /**
     * byte orientiert
     */

    // store encrypted document
    void encryptAndPersistDocument(
            DocumentKeyIDWithKey documentKeyIDWithKey,
            DocumentBucketPath documentBucketPath,
            OverwriteFlag overwriteFlag,
            Payload payload);

    // store unencrypted document
    void persistDocument(
            DocumentBucketPath documentBucketPath,
            OverwriteFlag overwriteFlag,
            Payload payload);

    // read encrypted document
    Payload loadAndDecryptDocument(
            StorageMetadata storageMetadata,
            KeyStoreAccess keyStoreAccess,
            DocumentBucketPath documentBucketPath);

    // read unencrypted document
    Payload loadDocument(
            StorageMetadata storageMetadata,
            DocumentBucketPath documentBucketPath);

    /**
     * stream orientiert
     */

    // store encrypted document stream
    void encryptAndPersistDocumentStream(
            DocumentKeyIDWithKey documentKeyIDWithKey,
            DocumentBucketPath documentBucketPath,
            OverwriteFlag overwriteFlag,
            PayloadStream payloadStream);

    // store unencrypted document stream
    void persistDocumentStream(
            DocumentBucketPath documentBucketPath,
            OverwriteFlag overwriteFlag,
            PayloadStream payloadStream);

    // read encrypted document stream
    PayloadStream loadAndDecryptDocumentStream(
            StorageMetadata storageMetadata,
            KeyStoreAccess keyStoreAccess,
            DocumentBucketPath documentBucketPath);

    // read unencrypted document stream
    PayloadStream loadDocumentStream(
            StorageMetadata storageMetadata,
            DocumentBucketPath documentBucketPath);

    static boolean isNotEncrypted(UserMetaData userMetaData) {
        String value = null;
        if ((value = userMetaData.find("NO_ENCRYPTION")) != null) {
            return (value.equalsIgnoreCase("TRUE"));
        }
        return false;
    }
    static void setNotEncrypted(UserMetaData userMetaData) {
        userMetaData.put("NO_ENCRYPTION", "TRUE");
    }


}
