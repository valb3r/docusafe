package org.adorsys.docusafe.business.v2;

import lombok.extern.slf4j.Slf4j;
import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.impl.DocumentSafeServiceImpl;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DSDocumentStream;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.Security;

@Slf4j
public class DocumentStreamTest {
    private final static ExtendedStoreConnection extendedStoreConnection = ExtendedStoreConnectionFactory.get();
    private DocumentSafeService documentSafeService = new DocumentSafeServiceImpl(extendedStoreConnection);
    private UserIDAuth userIDAuth;

    @BeforeClass
    static public void beforeClass() {
        log.debug("add bouncy castle provider");
        Security.addProvider(new BouncyCastleProvider());
        log.debug("clear whole database");
        extendedStoreConnection.listAllBuckets().forEach(bucket -> extendedStoreConnection.deleteContainer(bucket));
    }

    @Before
    public void setUp() {
        userIDAuth = new UserIDAuth(new UserID("test"), new ReadKeyPassword("password"));
        documentSafeService.createUser(userIDAuth);
    }

    @Test
    public void saveAndReadStreamTest() throws Exception {
        DocumentFQN documentFQN = new DocumentFQN("VeryBigDocument.txt");
        DSDocumentMetaInfo mi = new DSDocumentMetaInfo();

        String content = "test stream content";
        try (ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes())) {
            DSDocumentStream dsDocumentStream = new DSDocumentStream(documentFQN, bis, mi);

            documentSafeService.storeDocumentStream(userIDAuth, dsDocumentStream);
            log.info("successfully stored stream content: " + content);
        }

        String readContent;
        DSDocumentStream dsReadDocumentStream = documentSafeService.readDocumentStream(userIDAuth, documentFQN);
        try (InputStream is = dsReadDocumentStream.getDocumentStream()) {
            readContent = IOUtils.toString(is, Charset.defaultCharset());
            log.info("successfully read stream content: " + readContent);
        }

        Assert.assertEquals(content, readContent);
    }
}
