package org.adorsys.docusafe.cached.transactional.impl;

import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.impl.DocumentSafeServiceImpl;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.cached.transactional.CachedTransactionalFileStorage;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.docusafe.transactional.RequestMemoryContext;
import org.adorsys.docusafe.transactional.TransactionalFileStorage;
import org.adorsys.docusafe.transactional.impl.TransactionalFileStorageImpl;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 09.07.18 at 13:52.
 */
/*
    void createUser(UserIDAuth userIDAuth);
            void destroyUser(UserIDAuth userIDAuth);
            boolean userExists(UserID userID);
            void grantAccess(UserIDAuth userIDAuth, UserID receiverUserID);
            void storeDocument(UserIDAuth userIDAuth, UserID documentOwner, DSDocument dsDocument);
            DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);
            void deleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN);
            BucketContentFQN listDocuments(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag);

            void beginTransaction(UserIDAuth userIDAuth);
            void txStoreDocument(DSDocument dsDocument);
            DSDocument txReadDocument(DocumentFQN documentFQN);
            void txDeleteDocument(DocumentFQN documentFQN);
            BucketContentFQN txListDocuments(DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag);
            boolean txDocumentExists(DocumentFQN documentFQN);
            void txDeleteFolder(DocumentDirectoryFQN documentDirectoryFQN);
            void endTransaction();
*/
public class CachedTransactionalStorageTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(CachedTransactionalStorageTest.class);
    private DocumentSafeService documentSafeService = new DocumentSafeServiceImpl(ExtendedStoreConnectionFactory.get());

    List<UserIDAuth> userIDAuthList = new ArrayList<>();
    @Before
    public void before() {
        userIDAuthList.clear();
    }

    @After
    public void after() {
        userIDAuthList.forEach(userIDAuth -> documentSafeService.destroyUser(userIDAuth));
    }

    @Test
    public void simpleTestForReadAndStore() {
        RequestMemoryContext memoryContext = new SimpleRequestMemoryContextImpl();
        TransactionalFileStorage transactionalFileStorage = new TransactionalFileStorageImpl(memoryContext, documentSafeService);
        TransactionalStorageTestWrapper wrapper = new TransactionalStorageTestWrapper(transactionalFileStorage);
        CachedTransactionalFileStorage service = new CachedTransactionalFileStorageImpl(memoryContext, wrapper);

        UserIDAuth userIDAuth = new UserIDAuth(new UserID("peter"), new ReadKeyPassword("petersPassword"));
        DocumentFQN documentFQN = new DocumentFQN("folder1/file1.txt");
        service.createUser(userIDAuth);
        userIDAuthList.add(userIDAuth);
        service.beginTransaction(userIDAuth);

        // document speichern
        {
            DSDocument dsDocumentWrite = new DSDocument(
                    documentFQN,
                    new DocumentContent("content of file".getBytes()),
                    new DSDocumentMetaInfo()
            );
            service.txStoreDocument(dsDocumentWrite);
            DSDocument dsDocumentRead = service.txReadDocument(documentFQN);
            Assert.assertArrayEquals(dsDocumentWrite.getDocumentContent().getValue(), dsDocumentRead.getDocumentContent().getValue());
        }

        // Document überschreiben
        {
            DSDocument dsDocumentWrite = new DSDocument(
                    documentFQN,
                    new DocumentContent("another content of file".getBytes()),
                    new DSDocumentMetaInfo()
            );
            service.txStoreDocument(dsDocumentWrite);
            DSDocument dsDocumentRead = service.txReadDocument(documentFQN);
            Assert.assertArrayEquals(dsDocumentWrite.getDocumentContent().getValue(), dsDocumentRead.getDocumentContent().getValue());
        }

        service.endTransaction();
        LOGGER.debug(wrapper.toString());
        Assert.assertEquals(new Integer(1), wrapper.counterMap.get(TransactionalStorageTestWrapper.STORE_DOCUMENT_TX));
        Assert.assertEquals(new Integer(0), wrapper.counterMap.get(TransactionalStorageTestWrapper.READ_DOCUMENT_TX));
    }
}
