package org.adorsys.docusafe.transactional;

import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.docusafe.business.impl.DocumentSafeServiceImpl;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.docusafe.transactional.impl.TransactionalFileStorageImpl;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Security;

/**
 * Created by peter on 13.06.18 at 19:42.
 */
public class FileToInputBoxTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(FileToInputBoxTest.class);
    private TransactionalFileStorage transactionalFileStorage = new TransactionalFileStorageImpl(new DocumentSafeServiceImpl(ExtendedStoreConnectionFactory.get()));
    private UserIDAuth userIDAuth = new UserIDAuth(new UserID("peter"), new ReadKeyPassword("password"));
    private UserIDAuth systemUserIDAuth = new UserIDAuth(new UserID("system"), new ReadKeyPassword("systemPassword"));

    @Before
    public void preTest() {
        Security.addProvider(new BouncyCastleProvider());
        if (transactionalFileStorage.userExists(userIDAuth.getUserID())) {
            transactionalFileStorage.destroyUser(userIDAuth);
        }
        if (transactionalFileStorage.userExists(systemUserIDAuth.getUserID())) {
            transactionalFileStorage.destroyUser(systemUserIDAuth);
        }
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void testCreateUsersAndSendOneDocument() {
        transactionalFileStorage.createUser(userIDAuth);
        transactionalFileStorage.createUser(systemUserIDAuth);
        transactionalFileStorage.grantAccessToUserForInboxFolder(userIDAuth, systemUserIDAuth.getUserID());

        DocumentFQN documentFQN = new DocumentFQN("first.txt");
        DocumentContent documentContent = new DocumentContent("content for in put box".getBytes());
        DSDocumentMetaInfo documentMetaInfo = new DSDocumentMetaInfo();
        DSDocument document = new DSDocument(documentFQN, documentContent, documentMetaInfo);

        transactionalFileStorage.storeDocumentInInputFolder(systemUserIDAuth, userIDAuth.getUserID(), document);
    }
}