package org.adorsys.documentsafe.layer03business.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.KeyStoreDirectory;
import org.adorsys.documentsafe.layer02service.BucketService;
import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer02service.types.PlainFileContent;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentDirectory;
import org.adorsys.documentsafe.layer03business.exceptions.GuardException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 19.01.18 at 16:13.
 */
public class GuardUtil {
    private final static Logger LOGGER = LoggerFactory.getLogger(GuardUtil.class);
    private final static String BUCKET_GUARD_KEY = ".bucketGuardKey";

    public static void saveBucketGuardKeyFile(BucketService bucketService, KeyStoreDirectory keyStoreDirectory, DocumentDirectory documentDirectory, DocumentKeyID documentKeyID) {
        BucketPath guardFile = keyStoreDirectory.append(documentDirectory).add(BUCKET_GUARD_KEY);
        Gson gson = new GsonBuilder().create();
        String jsonString = gson.toJson(documentKeyID);
        PlainFileContent plainFileContent = new PlainFileContent(jsonString.getBytes());
        bucketService.createPlainFile(guardFile, plainFileContent);
    }

    public static DocumentKeyID tryToLoadBucketGuardKeyFile(BucketService bucketService, KeyStoreDirectory keyStoreDirectory, BucketPath bucketPath) {
        BucketPath guardFile = keyStoreDirectory.append(bucketPath).add(BUCKET_GUARD_KEY);
        if (!bucketService.existsFile(guardFile)) {
            return null;
        }
        PlainFileContent plainFileContent = bucketService.readPlainFile(guardFile);
        Gson gson = new GsonBuilder().create();
        DocumentKeyID documentKeyID = gson.fromJson(new String(plainFileContent.getValue()), DocumentKeyID.class);
        return documentKeyID;
    }

    public static DocumentKeyID loadBucketGuardKeyFile(BucketService bucketService, KeyStoreDirectory keyStoreDirectory, DocumentDirectory documentDirectory) {
        DocumentKeyID documentKeyID = tryToLoadBucketGuardKeyFile(bucketService, keyStoreDirectory, documentDirectory);
        if (documentKeyID == null) {
            throw new GuardException("No DocumentGuard found for Bucket" + documentDirectory);
        }
        return documentKeyID;
    }

}