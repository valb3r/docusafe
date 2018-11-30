package org.adorsys.docusafe.transactional;

import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 28.11.18 17:11.
 */
public class LazyUserCreationTest extends TransactionFileStorageBaseTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(TransactionalFileStorageTest.class);

    // @Test
    public void createUserAfterBegin() {

        transactionalFileStorage.beginTransaction(userIDAuth);
        transactionalFileStorage.createUser(userIDAuth);
        DocumentFQN documentFQN = new DocumentFQN("testxTFolder/first.txt");
        DocumentContent documentContent = new DocumentContent("very first".getBytes());
        DSDocumentMetaInfo documentMetaInfo = new DSDocumentMetaInfo();
        DSDocument document = new DSDocument(documentFQN, documentContent, documentMetaInfo);

        Assert.assertFalse(transactionalFileStorage.txDocumentExists(userIDAuth, documentFQN));
        transactionalFileStorage.txStoreDocument(userIDAuth, document);
        Assert.assertTrue(transactionalFileStorage.txDocumentExists(userIDAuth, documentFQN));
        transactionalFileStorage.endTransaction(userIDAuth);
    }
}
