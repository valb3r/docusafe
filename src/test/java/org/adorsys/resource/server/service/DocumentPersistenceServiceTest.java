package org.adorsys.resource.server.service;

import org.adorsys.encobject.service.BlobStoreConnection;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.adorsys.encobject.utils.TestFsBlobStoreFactory;
import org.adorsys.encobject.utils.TestKeyUtils;
import org.adorsys.resource.server.basetypes.DocumentContent;
import org.adorsys.resource.server.basetypes.DocumentGuardName;
import org.adorsys.resource.server.basetypes.DocumentID;
import org.adorsys.resource.server.persistence.ExtendedObjectPersistence;
import org.adorsys.resource.server.persistence.basetypes.BucketName;

import javax.security.auth.callback.CallbackHandler;

/**
 * Created by peter on 02.01.18.
 */
public class DocumentPersistenceServiceTest {
    private static BlobStoreContextFactory documentContextFactory;
    private static ExtendedObjectPersistence documentExtendedPersistence;

    public static void beforeClass() {
        TestKeyUtils.turnOffEncPolicy();

        documentContextFactory = new TestFsBlobStoreFactory();
        documentExtendedPersistence = new ExtendedObjectPersistence(new BlobStoreConnection(documentContextFactory));
    }
    public static void afterClass() {

    }

    public void testPersistDocument(DocumentGuardService documentGuardService,
                                    CallbackHandler userKeystoreHandler,
                                    CallbackHandler keyPassHandler,
                                    DocumentGuardName documentGuardName,
                                    BucketName documentBucketName,
                                    DocumentID documentID,
                                    DocumentContent documentContent) {
        DocumentPersistenceService documentPersistenceService = new DocumentPersistenceService(documentExtendedPersistence, documentGuardService);
        documentPersistenceService.persistDocument(
                userKeystoreHandler,
                keyPassHandler,
                documentGuardName,
                documentBucketName,
                documentID,
                documentContent);
        }
}
