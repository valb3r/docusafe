package org.adorsys.resource.server.service;

import org.adorsys.resource.server.basetypes.BucketName;
import org.adorsys.resource.server.basetypes.DocumentContent;
import org.adorsys.resource.server.basetypes.DocumentID;
import org.adorsys.resource.server.basetypes.KeyStoreName;
import org.adorsys.resource.server.basetypes.UserID;
import org.adorsys.resource.server.complextypes.KeyStore;
import org.adorsys.resource.server.exceptions.ServiceException;

/**
 * Created by peter on 23.12.17 at 17:18.
 */
public class PersistenceService {
    S3ContainerInterface s3Service;

    public DocumentID persistDocument(UserID caller, UserID destination, BucketName bucketName, DocumentContent documentContent) {
        KeyStoreName keyStoreName = KeyStore.getKeyStoreName(destination);
        DocumentID keyStoreDocumentID = new DocumentID(keyStoreName.getValue());
        KeyStore keyStore = new KeyStore(keyStoreName, s3Service.getDocument(keyStoreDocumentID));

        if (caller.equals(destination)) {
            // Es reicht einen symmetrischen Schlüssel für den Guard zu benutzen
        } else {
            // Der Guard muss mit dem PublicKey des destination Users verschlüsselt werden
        }

        throw new ServiceException("NYI");
    }
}
