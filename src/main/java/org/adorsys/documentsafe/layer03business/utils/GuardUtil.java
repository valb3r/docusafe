package org.adorsys.documentsafe.layer03business.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.adorsys.documentsafe.layer01persistence.types.ListRecursiveFlag;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.KeyStoreBucketPath;
import org.adorsys.documentsafe.layer02service.BucketService;
import org.adorsys.documentsafe.layer02service.types.DocumentContent;
import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer02service.types.PlainFileContent;
import org.adorsys.documentsafe.layer02service.types.PlainFileName;
import org.adorsys.documentsafe.layer02service.types.complextypes.BucketContent;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAuth;
import org.adorsys.documentsafe.layer03business.exceptions.GuardException;
import org.adorsys.documentsafe.layer03business.types.UserHomeBucketPath;
import org.adorsys.documentsafe.layer03business.types.UserID;
import org.adorsys.documentsafe.layer03business.types.complex.UserIDAuth;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.StringTokenizer;

/**
 * Created by peter on 19.01.18 at 16:13.
 */
public class GuardUtil {
    private final static Logger LOGGER = LoggerFactory.getLogger(GuardUtil.class);
    private final static String BUCKET_GUARD_KEY = ".bucketGuardKey";

    public static void createBucketGuardKeyFile(BucketService bucketService, KeyStoreBucketPath keyStoreBucketPath, BucketPath bucketPath, DocumentKeyID documentKeyID) {
        PlainFileName plainFileName = new PlainFileName(bucketPath.getObjectHandlePath() + BUCKET_GUARD_KEY);
        Gson gson = new GsonBuilder().create();
        String jsonString = gson.toJson(documentKeyID);
        PlainFileContent plainFileContent = new PlainFileContent(jsonString.getBytes());
        bucketService.createPlainFile(keyStoreBucketPath, plainFileName, plainFileContent);
    }

    public static DocumentKeyID tryToReadBucketGuardKeyFile(BucketService bucketService, KeyStoreBucketPath keyStoreBucketPath, BucketPath bucketPath) {
        PlainFileName plainFileName = new PlainFileName(bucketPath.getObjectHandlePath() + BUCKET_GUARD_KEY);
        if (!bucketService.existsFile(keyStoreBucketPath, plainFileName)) {
            return null;
        }
        PlainFileContent plainFileContent = bucketService.readPlainFile(keyStoreBucketPath, plainFileName);
        Gson gson = new GsonBuilder().create();
        DocumentKeyID documentKeyID = gson.fromJson(new String(plainFileContent.getValue()), DocumentKeyID.class);
        return documentKeyID;
    }

    public static DocumentKeyID readBucketGuardKeyFile(BucketService bucketService, KeyStoreBucketPath keyStoreBucketPath, BucketPath bucketPath) {
        DocumentKeyID documentKeyID = tryToReadBucketGuardKeyFile(bucketService, keyStoreBucketPath, bucketPath);
        if (documentKeyID == null) {
            throw new GuardException("No DocumentGuard found for Bucket" + bucketPath.getObjectHandlePath());
        }
        return documentKeyID;
    }

}