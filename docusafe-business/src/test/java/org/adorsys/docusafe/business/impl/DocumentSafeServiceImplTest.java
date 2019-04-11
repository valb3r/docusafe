package org.adorsys.docusafe.business.impl;

import lombok.SneakyThrows;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.Payload;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.UserMetaData;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.adorsys.encobject.service.impl.SimplePayloadImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class DocumentSafeServiceImplTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DocumentSafeServiceImplTest.class);

    private Map<BucketPath, Payload> blobPayload = new HashMap<>();
    private Map<BucketPath, String> blobBytes = new HashMap<>();
    private Map<BucketPath, StorageMetadata> meta = new HashMap<>();

    private DocusafeCacheWrapper cacheWrapper = new DocusafeCacheWrapperImpl(CacheType.HASH_MAP);
    private ExtendedStoreConnection connection = mock(ExtendedStoreConnection.class);
    private DocumentSafeServiceImpl safeService = new DocumentSafeServiceImpl(cacheWrapper, connection);

    @Test
    public void storeDocumentStream() {
        UserIDAuth auth = new UserIDAuth(new UserID("1"), new ReadKeyPassword("2"));
        DSDocument dsDocument = new DSDocument(new DocumentFQN("2/foo.bar"), new DocumentContent("a".getBytes()), null);
        DSDocument dsDocumentAnother = new DSDocument(new DocumentFQN("2/foo1.bar"), new DocumentContent("b".getBytes()), null);
        AtomicInteger keyPutCount = new AtomicInteger();

        prepareFS();


        safeService.createUser(auth);
        log.info("Store document");

        ExecutorService svc = Executors.newFixedThreadPool(2);

        svc.submit(() -> safeService.storeDocument(auth, dsDocument));
        svc.submit(() -> safeService.storeDocument(auth, dsDocumentAnother));

        //cacheWrapper.getDocumentGuardCache().clear();
        //cacheWrapper.getDocumentKeyIDCache().clear();

        svc.shutdown();
        try {
            svc.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (Exception ex) {

        }

        cacheWrapper.getDocumentGuardCache().clear();
        cacheWrapper.getDocumentKeyIDCache().clear();
        cacheWrapper.getUserAuthCache().clear();

        log.info("Read data");
        safeService.readDocument(auth, dsDocument.getDocumentFQN());
    }

    private String unroll(UserMetaData meta) {
        return meta.keySet().stream().map(it -> it + ":" + meta.get(it)).collect(Collectors.joining(","));
    }

    private void prepareFS() {
        doAnswer(inv -> {
            BucketPath path = inv.getArgumentAt(0, BucketPath.class);
            Payload payload = inv.getArgumentAt(1, Payload.class);
            log.info("PUT-payload to {} with meta {}", path, unroll(payload.getStorageMetadata().getUserMetadata()));
            blobPayload.put(path, payload);
            meta.put(path, payload.getStorageMetadata());
            return null;
        }).when(connection).putBlob(any(), any(Payload.class));

        doAnswer(inv -> {
            BucketPath path = inv.getArgumentAt(0, BucketPath.class);
            log.info("GET-payload to {}", path);
            Payload blob = blobPayload.get(path);
            if (blob != null) {
                return blob;
            }
            if (null != blobBytes.get(path)) {
                return new SimplePayloadImpl(blobBytes.get(path).getBytes());
            }
            return null;
        }).when(connection).getBlob(any());

        doAnswer(inv -> {
            BucketPath path = inv.getArgumentAt(0, BucketPath.class);
            log.info("GET-payload to {}", path);
            Payload blob = blobPayload.get(path);
            if (blob != null) {
                return blob;
            }
            if (null != blobBytes.get(path)) {
                return new SimplePayloadImpl(blobBytes.get(path).getBytes());
            }
            return null;
        }).when(connection).getBlob(any(), any());

        AtomicInteger gpkCounter = new AtomicInteger();
        doAnswer(inv -> {
            BucketPath path = inv.getArgumentAt(0, BucketPath.class);
            byte[] payload = inv.getArgumentAt(1, byte[].class);
            log.info("PUT-bytes to {} with meta {}", path, new String(payload));
            blobBytes.put(path, new String(payload));

            if (gpkCounter.getAndIncrement() == 1) {
                cacheWrapper.getDocumentGuardCache().clear();
                cacheWrapper.getDocumentKeyIDCache().clear();
            }

            return null;
        }).when(connection).putBlob(any(), any(byte[].class));

        doAnswer(inv -> {
            BucketPath path = inv.getArgumentAt(0, BucketPath.class);
            log.info("GET-metadata to {}", path);
            Payload blob = blobPayload.get(path);
            if (blob != null) {
                return blob.getStorageMetadata();
            }
            if (null != meta.get(path)) {
                return meta.get(path);
            }

            return new SimpleStorageMetadataImpl();
        }).when(connection).getStorageMetadata(any());

        doAnswer(inv -> {
            BucketPath path = inv.getArgumentAt(0, BucketPath.class);
            boolean eval = false;

            Payload blob = blobPayload.get(path);
            if (blob != null) {
                eval = true;
            } else {
                eval = null != blobBytes.get(path);
            }

            log.info("IS EXIST {} is {}", path, eval);
            return eval;


        }).when(connection).blobExists(any());
    }
}
