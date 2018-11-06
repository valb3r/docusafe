package org.adorsys.docusafe.cached.transactional.impl;

import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.docusafe.business.impl.DocumentSafeServiceImpl;
import org.adorsys.docusafe.business.impl.WithCache;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.cached.transactional.CachedTransactionalDocumentSafeService;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.docusafe.transactional.RequestMemoryContext;
import org.adorsys.docusafe.transactional.impl.TransactionalDocumentSafeServiceImpl;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.adorsys.encobject.types.ListRecursiveFlag;
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

public class CachedTransactionalStorageTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(CachedTransactionalStorageTest.class);
    private RequestMemoryContext memoryContext = new SimpleRequestMemoryContextImpl();
    private TransactionalStorageTestWrapper wrapper = new TransactionalStorageTestWrapper(
            new TransactionalDocumentSafeServiceImpl(memoryContext,
                    new DocumentSafeServiceImpl(WithCache.FALSE, ExtendedStoreConnectionFactory.get())));
    private CachedTransactionalDocumentSafeService service = new CachedTransactionalDocumentSafeServiceImpl(memoryContext, wrapper);

    List<UserIDAuth> userIDAuthList = new ArrayList<>();
    @Before
    public void before() {
        userIDAuthList.clear();
    }

    @After
    public void after() {
        userIDAuthList.forEach(userIDAuth -> service.destroyUser(userIDAuth));
    }

    @Test
    public void testTxListAndDeleteDocument() {
        

        UserIDAuth userIDAuth = new UserIDAuth(new UserID("peter"), new ReadKeyPassword("petersPassword"));
        service.createUser(userIDAuth);
        userIDAuthList.add(userIDAuth);

        DocumentFQN documentFQN = new DocumentFQN("folder1/file1.txt");
        service.beginTransaction(userIDAuth);
        Assert.assertEquals(new Integer(0), wrapper.counterMap.get(TransactionalStorageTestWrapper.LIST_DOCUMENTS_TX));
        BucketContentFQN bucketContentFQN = service.txListDocuments(userIDAuth, documentFQN.getDocumentDirectory(), ListRecursiveFlag.TRUE);
        Assert.assertTrue(bucketContentFQN.getFiles().isEmpty());
        Assert.assertTrue(bucketContentFQN.getDirectories().isEmpty());
        Assert.assertFalse(service.txDocumentExists(userIDAuth, documentFQN));

        // document speichern
        {
            DSDocument dsDocumentWrite = new DSDocument(
                    documentFQN,
                    new DocumentContent("content of file".getBytes()),
                    new DSDocumentMetaInfo()
            );
            service.txStoreDocument(userIDAuth, dsDocumentWrite);
            DSDocument dsDocumentRead = service.txReadDocument(userIDAuth, documentFQN);
            Assert.assertArrayEquals(dsDocumentWrite.getDocumentContent().getValue(), dsDocumentRead.getDocumentContent().getValue());
        }
        BucketContentFQN bucketContentFQN2 = service.txListDocuments(userIDAuth, documentFQN.getDocumentDirectory(), ListRecursiveFlag.TRUE);
        Assert.assertEquals(1, bucketContentFQN2.getFiles().size());
        Assert.assertTrue(bucketContentFQN2.getDirectories().isEmpty());
        Assert.assertTrue(service.txDocumentExists(userIDAuth, documentFQN));

        Assert.assertEquals(new Integer(0), wrapper.counterMap.get(TransactionalStorageTestWrapper.STORE_DOCUMENT_TX));
        Assert.assertEquals(new Integer(0), wrapper.counterMap.get(TransactionalStorageTestWrapper.READ_DOCUMENT_TX));
        Assert.assertEquals(new Integer(1), wrapper.counterMap.get(TransactionalStorageTestWrapper.LIST_DOCUMENTS_TX));
        service.endTransaction(userIDAuth);
        LOGGER.debug(wrapper.toString());
        Assert.assertEquals(new Integer(1), wrapper.counterMap.get(TransactionalStorageTestWrapper.STORE_DOCUMENT_TX));
        Assert.assertEquals(new Integer(0), wrapper.counterMap.get(TransactionalStorageTestWrapper.READ_DOCUMENT_TX));
        Assert.assertEquals(new Integer(1), wrapper.counterMap.get(TransactionalStorageTestWrapper.LIST_DOCUMENTS_TX));
        Assert.assertEquals(documentFQN, bucketContentFQN2.getFiles().get(0));
    }

    @Test
    public void testTxReadAndStore() {
        

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
            service.txStoreDocument(userIDAuth, dsDocumentWrite);
            DSDocument dsDocumentRead = service.txReadDocument(userIDAuth, documentFQN);
            Assert.assertArrayEquals(dsDocumentWrite.getDocumentContent().getValue(), dsDocumentRead.getDocumentContent().getValue());
        }

        // Document überschreiben
        {
            DSDocument dsDocumentWrite = new DSDocument(
                    documentFQN,
                    new DocumentContent("another content of file".getBytes()),
                    new DSDocumentMetaInfo()
            );
            service.txStoreDocument(userIDAuth, dsDocumentWrite);
            DSDocument dsDocumentRead = service.txReadDocument(userIDAuth, documentFQN);
            Assert.assertArrayEquals(dsDocumentWrite.getDocumentContent().getValue(), dsDocumentRead.getDocumentContent().getValue());
        }

        Assert.assertEquals(new Integer(0), wrapper.counterMap.get(TransactionalStorageTestWrapper.STORE_DOCUMENT_TX));
        Assert.assertEquals(new Integer(0), wrapper.counterMap.get(TransactionalStorageTestWrapper.READ_DOCUMENT_TX));
        service.endTransaction(userIDAuth);
        LOGGER.debug(wrapper.toString());
        Assert.assertEquals(new Integer(1), wrapper.counterMap.get(TransactionalStorageTestWrapper.STORE_DOCUMENT_TX));
        Assert.assertEquals(new Integer(0), wrapper.counterMap.get(TransactionalStorageTestWrapper.READ_DOCUMENT_TX));
    }


    @Test
    public void testNonTxListAndDeleteDocument() {
        

        UserIDAuth userIDAuth = new UserIDAuth(new UserID("peter"), new ReadKeyPassword("petersPassword"));
        service.createUser(userIDAuth);
        userIDAuthList.add(userIDAuth);

        UserIDAuth userIDAuth2 = new UserIDAuth(new UserID("francis"), new ReadKeyPassword("francisPassword"));
        service.createUser(userIDAuth2);
        userIDAuthList.add(userIDAuth2);

        DocumentDirectoryFQN francisFolder = new DocumentDirectoryFQN("francis");
        service.grantAccessToNonTxFolder(userIDAuth, userIDAuth2.getUserID(), francisFolder);
        DocumentFQN documentFQN = francisFolder.addName("file1.txt");
        BucketContentFQN bucketContentFQN = service.nonTxListDocuments(userIDAuth, documentFQN.getDocumentDirectory(), ListRecursiveFlag.TRUE);
        Assert.assertTrue(bucketContentFQN.getFiles().isEmpty());
        Assert.assertTrue(bucketContentFQN.getDirectories().isEmpty());
        Assert.assertFalse(service.nonTxDocumentExists(userIDAuth, documentFQN));
        Assert.assertFalse(service.nonTxDocumentExists(userIDAuth2, userIDAuth.getUserID(), documentFQN));

        // document speichern
        {
            DSDocument dsDocumentWrite = new DSDocument(
                    documentFQN,
                    new DocumentContent("content of file".getBytes()),
                    new DSDocumentMetaInfo()
            );
            service.nonTxStoreDocument(userIDAuth2, userIDAuth.getUserID(), dsDocumentWrite);
            DSDocument dsDocumentRead = service.nonTxReadDocument(userIDAuth, documentFQN);
            Assert.assertArrayEquals(dsDocumentWrite.getDocumentContent().getValue(), dsDocumentRead.getDocumentContent().getValue());
        }
        BucketContentFQN bucketContentFQN2 = service.nonTxListDocuments(userIDAuth, documentFQN.getDocumentDirectory(), ListRecursiveFlag.TRUE);
        Assert.assertEquals(1, bucketContentFQN2.getFiles().size());
        Assert.assertTrue(bucketContentFQN2.getDirectories().isEmpty());
        Assert.assertTrue(service.nonTxDocumentExists(userIDAuth2, userIDAuth.getUserID(), documentFQN));
        Assert.assertTrue(service.nonTxDocumentExists(userIDAuth, documentFQN));

        LOGGER.debug(wrapper.toString());
        Assert.assertEquals(new Integer(1), wrapper.counterMap.get(TransactionalStorageTestWrapper.STORE_GRANTED_DOCUMENT));
        Assert.assertEquals(new Integer(0), wrapper.counterMap.get(TransactionalStorageTestWrapper.STORE_DOCUMENT));
        Assert.assertEquals(new Integer(1), wrapper.counterMap.get(TransactionalStorageTestWrapper.READ_DOCUMENT));
        Assert.assertEquals(new Integer(2), wrapper.counterMap.get(TransactionalStorageTestWrapper.LIST_DOCUMENTS));
        Assert.assertEquals(new Integer(2), wrapper.counterMap.get(TransactionalStorageTestWrapper.GRANTED_DOCUMENT_EXISTS));
        Assert.assertEquals(documentFQN, bucketContentFQN2.getFiles().get(0));
    }
}
