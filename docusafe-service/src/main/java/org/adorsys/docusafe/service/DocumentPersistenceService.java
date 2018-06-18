package org.adorsys.docusafe.service;

import org.adorsys.docusafe.service.types.complextypes.DocumentBucketPath;
import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.encobject.domain.KeyStoreAccess;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.PayloadStream;
import org.adorsys.encobject.types.OverwriteFlag;

/**
 * Created by peter on 11.01.18.
 */
public interface DocumentPersistenceService {
    public final static String NO_ENCRYPTION = "NO_ENCRYPTION";

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

    // load document, if necessary, decrypt it
    Payload loadDecryptedDocument(
            KeyStoreAccess keyStoreAccess,
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

    // load document stream, if necessary, decrypt it
    PayloadStream loadDecryptedDocumentStream(
            KeyStoreAccess keyStoreAccess,
            DocumentBucketPath documentBucketPath);


}
