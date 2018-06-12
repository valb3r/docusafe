package org.adorsys.docusafe.transactional;

import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.cryptoutils.storeconnectionfactory.StoreConnectionFactoryConfig;
import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.impl.DocumentSafeServiceImpl;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.docusafe.transactional.impl.FileStorageImpl;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.security.Security;

/**
 * Created by peter on 12.06.18 at 08:44.
 */
public class FileStorageTest {
    private ExtendedStoreConnection esc = ExtendedStoreConnectionFactory.get();
    private DocumentSafeService documentSafeService = new DocumentSafeServiceImpl(esc);
    private FileStorage fileStorage = new FileStorageImpl(new DocumentSafeServiceImpl(esc));
    private UserIDAuth userIDAuth = new UserIDAuth(new UserID("peter"), new ReadKeyPassword("password"));

    @Before
    public void preTest() {
        Security.addProvider(new BouncyCastleProvider());
        if (documentSafeService.userExists(userIDAuth.getUserID())) {
            documentSafeService.destroyUser(userIDAuth);
        }
    }

    @Test
    public void test() {
        documentSafeService.createUser(userIDAuth);
        DocumentFQN documentFQN = new DocumentFQN("peter/first.txt");
        DocumentContent documentContent = new DocumentContent("very first".getBytes());
        DSDocumentMetaInfo documentMetaInfo = new DSDocumentMetaInfo();
        DSDocument document = new DSDocument(documentFQN, documentContent, documentMetaInfo);

        {
            TxID txid = fileStorage.beginTransaction(userIDAuth);
            Assert.assertFalse(fileStorage.documentExists(txid, userIDAuth, documentFQN));
            fileStorage.storeDocument(txid, userIDAuth, document);
            Assert.assertTrue(fileStorage.documentExists(txid, userIDAuth, documentFQN));
            fileStorage.endTransaction(txid, userIDAuth);
        }
    }
}
