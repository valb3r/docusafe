package org.adorsys.documentsafe.layer03business;

import org.adorsys.documentsafe.layer00common.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer01persistence.ExtendedBlobStoreConnection;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.adorsys.documentsafe.layer02service.types.DocumentID;
import org.adorsys.documentsafe.layer02service.types.ReadKeyPassword;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentBucketPath;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentLocation;
import org.adorsys.documentsafe.layer02service.utils.TestFsBlobStoreFactory;
import org.adorsys.documentsafe.layer03business.impl.DocumentSafeServiceImpl;
import org.adorsys.documentsafe.layer03business.types.UserID;
import org.adorsys.documentsafe.layer03business.types.complex.UserIDAuth;
import org.adorsys.documentsafe.layer03business.utils.UserIDUtil;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.adorsys.encobject.service.ContainerPersistence;
import org.junit.Test;

/**
 * Created by peter on 19.01.18 at 16:25.
 */
public class TestBusiness {
    private final static BlobStoreContextFactory factory = new TestFsBlobStoreFactory();

    public void after(BucketPath bucket) {
        try {
            ContainerPersistence containerPersistence = new ContainerPersistence(new ExtendedBlobStoreConnection(factory));
            containerPersistence.deleteContainer(bucket.getFirstBucket().getValue());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void testCreateUser() {
        DocumentSafeService service = new DocumentSafeServiceImpl(factory);
        UserID userPeter = new UserID("UserPeter");
        UserIDAuth userIDAuth = new UserIDAuth(userPeter, new ReadKeyPassword("peterkey"));
        service.createUserID(userIDAuth);
        after(UserIDUtil.getBucketPath(userPeter));
    }

    // @Test
    public void loadCreateUser() {
        DocumentSafeService service = new DocumentSafeServiceImpl(factory);
        UserIDAuth userIDAuth = new UserIDAuth(new UserID("UserPeter"), new ReadKeyPassword("peterkey"));
        DocumentLocation documentLocation = new DocumentLocation(new DocumentID("README.txt"), new DocumentBucketPath(""));
        service.readDocument(userIDAuth, documentLocation);
    }

}
