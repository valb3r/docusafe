package org.adorsys.docusafe.business;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.docusafe.business.impl.DocumentSafeServiceImpl;
import org.adorsys.docusafe.business.impl.WithCache;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DSDocumentStream;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.business.utils.GuardUtil;
import org.adorsys.docusafe.business.utils.UserIDUtil;
import org.adorsys.docusafe.service.BucketService;
import org.adorsys.docusafe.service.impl.BucketServiceImpl;
import org.adorsys.docusafe.service.types.BucketContent;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.docusafe.service.types.DocumentKeyID;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.Security;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by peter on 20.06.18 at 10:06.
 */
public class BusinessTestBase {
    private final static Logger LOGGER = LoggerFactory.getLogger(BusinessTestBase.class);
    protected final static ExtendedStoreConnection extendedStoreConnection = ExtendedStoreConnectionFactory.get();
    protected DocumentSafeService service;
    protected WithCache withCache = WithCache.FALSE;

    public static Set<UserIDAuth> users = new HashSet<>();

    @BeforeClass
    static public void beforeClass() {
        LOGGER.debug("add bouncy castle provider");
        Security.addProvider(new BouncyCastleProvider());
        LOGGER.debug("clear whole database");
        extendedStoreConnection.listAllBuckets().forEach(bucket -> extendedStoreConnection.deleteContainer(bucket));
    }

    @Before
    public void before() {
        LOGGER.debug("add bouncy castle provider");
        Security.addProvider(new BouncyCastleProvider());
        users.clear();
        service = new DocumentSafeServiceImpl(withCache, extendedStoreConnection);
    }

    @After
    public void after() {
        try {
            LOGGER.debug("AFTER TEST:" + DocumentSafeServiceImpl.showCache(service));
            users.forEach(userIDAuth -> {
                LOGGER.debug("AFTER TEST DESTROY " + userIDAuth.getUserID().getValue());
                service.destroyUser(userIDAuth);
            });
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }


    protected void createDirectoryWithSubdirectories(int depth, UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, int numSubdires, int numFiles) {
        if (depth == 0) {
            return;
        }
        for (int i = 0; i < numFiles; i++) {
            DocumentFQN documentFQN = documentDirectoryFQN.addName("file_" + i);
            createDocument(userIDAuth, documentFQN);
        }
        for (int i = 0; i < numSubdires; i++) {
            DocumentDirectoryFQN subdir = documentDirectoryFQN.addDirectory("dir_" + i);
            createDirectoryWithSubdirectories(depth - 1, userIDAuth, subdir, numSubdires, numFiles);
        }
    }

    protected int getNumberOfGuards(UserID userID) {
        BucketService bucketService = new BucketServiceImpl(extendedStoreConnection);
        BucketDirectory keyStoreDirectory = UserIDUtil.getKeyStoreDirectory(userID);
        BucketContent bucketContent = bucketService.readDocumentBucket(keyStoreDirectory, ListRecursiveFlag.TRUE);
        int count = 0;
        for (StorageMetadata meta : bucketContent.getContent()) {
            if (meta.getName().endsWith(GuardUtil.BUCKET_GUARD_KEY)) {
                count++;
            }
        }
        return count;
    }


    protected UserIDAuth createUser(UserID userID) {
        return createUser(userID, new ReadKeyPassword("peterkey"));
    }

    protected UserIDAuth createUser() {
        return createUser(new UserID("peter"), new ReadKeyPassword("peterkey"));
    }

    protected UserIDAuth createUser(UserID userID, ReadKeyPassword readKeyPassword) {
        UserIDAuth userIDAuth = new UserIDAuth(userID, readKeyPassword);
        users.add(userIDAuth);
        service.createUser(userIDAuth);
        Assert.assertEquals("Anzahl der guards muss genau 1 sein", 1, getNumberOfGuards(userIDAuth.getUserID()));
        return userIDAuth;
    }

    protected DSDocument createDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        return createDocument(userIDAuth, documentFQN, null);

    }

    protected DSDocument createDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN, DSDocumentMetaInfo mi) {
        DSDocument dsDocument;
        DocumentContent documentContent = new DocumentContent("Einfach nur a bisserl Text".getBytes());
        dsDocument = new DSDocument(documentFQN, documentContent, mi);

        // check, there exists no guard yet
        LOGGER.debug("check no bucket guard exists yet for " + dsDocument.getDocumentFQN());
        service.storeDocument(userIDAuth, dsDocument);
        return dsDocument;
    }

    protected DSDocumentStream createDocumentStream(UserIDAuth userIDAuth, DocumentFQN documentFQN, DSDocumentMetaInfo mi) {
        try {
            String content = "Einfach nur a bisserl Text";
            try (ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes())) {
                DSDocumentStream dsDocumentStream = new DSDocumentStream(documentFQN, bis, mi);

                // check, there exists no guard yet
                LOGGER.debug("check no bucket guard exists yet for " + dsDocumentStream.getDocumentFQN());
                service.storeDocumentStream(userIDAuth, dsDocumentStream);

                // Der Stream wurde nun schon ausgelesen, um ihn später prüfen zu können, müsste er resetted werden
                // nicht alle streams sind resettable, daher hier auf nummer sicher gehen
                return service.readDocumentStream(userIDAuth, dsDocumentStream.getDocumentFQN());
            }
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    protected DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN, DocumentContent documentContent) {
        DSDocument dsDocumentResult = service.readDocument(userIDAuth, documentFQN);
        LOGGER.debug("original  document:" + new String(documentContent.getValue()));
        LOGGER.debug("retrieved document:" + new String(dsDocumentResult.getDocumentContent().getValue()));
        Assert.assertEquals("document content ok", documentContent, dsDocumentResult.getDocumentContent());
        return dsDocumentResult;
    }

    protected DSDocumentStream readDocumentStream(UserIDAuth userIDAuth, DocumentFQN documentFQN, InputStream origInputStream) {
        try {
            DSDocumentStream dsDocument1Result = service.readDocumentStream(userIDAuth, documentFQN);
            try (InputStream is2 = dsDocument1Result.getDocumentStream()) {
                String readContent = IOUtils.toString(is2, Charset.defaultCharset());
                String origContent = IOUtils.toString(origInputStream, Charset.defaultCharset());
                LOGGER.debug("original  document stream:" + origContent);
                LOGGER.debug("retrieved document:" + readContent);
                Assert.assertEquals("document content ok", origContent, readContent);
            }
            return dsDocument1Result;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    protected static void sleep(int secs) {
        try {
            LOGGER.debug("SLEEP FOR " + secs + " secs");
            Thread.sleep(secs * 1000);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }


}
