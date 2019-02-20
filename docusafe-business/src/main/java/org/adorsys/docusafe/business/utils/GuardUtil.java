package org.adorsys.docusafe.business.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.service.BucketService;
import org.adorsys.docusafe.service.exceptions.NoDocumentGuardExists;
import org.adorsys.docusafe.service.types.DocumentKeyID;
import org.adorsys.docusafe.service.types.PlainFileContent;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 19.01.18 at 16:13.
 */
public class GuardUtil {
    private final static Logger LOGGER = LoggerFactory.getLogger(GuardUtil.class);
    public final static String BUCKET_GUARD_KEY = ".bgk";

    public static void saveBucketGuardKeyFile(BucketService bucketService, BucketDirectory keyStoreDirectory, BucketDirectory documentDirectory, DocumentKeyID documentKeyID) {
        BucketPath guardFile = keyStoreDirectory.append(documentDirectory.addSuffix(BUCKET_GUARD_KEY));
        Gson gson = new GsonBuilder().create();
        String jsonString = gson.toJson(documentKeyID);
        PlainFileContent plainFileContent = new PlainFileContent(jsonString.getBytes());
        bucketService.createPlainFile(guardFile, plainFileContent);
    }

    public static void deleteBucketGuardKeyFile(BucketService bucketService, BucketDirectory keyStoreDirectory, BucketDirectory documentDirectory) {
        BucketPath guardFile = keyStoreDirectory.append(documentDirectory.addSuffix(BUCKET_GUARD_KEY));
        bucketService.deletePlainFile(guardFile);
    }

    public static DocumentKeyID tryToLoadBucketGuardKeyFile(BucketService bucketService, BucketDirectory keyStoreDirectory, BucketDirectory documentDirectory) {
        BucketPath guardFile = keyStoreDirectory.append(documentDirectory.addSuffix(BUCKET_GUARD_KEY));
        if (!bucketService.fileExists(guardFile)) {
            return null;
        }
        PlainFileContent plainFileContent = bucketService.readPlainFile(guardFile);
        Gson gson = new GsonBuilder().create();
        DocumentKeyID documentKeyID = gson.fromJson(new String(plainFileContent.getValue()), DocumentKeyID.class);
        return documentKeyID;
    }

    public static DocumentKeyID loadBucketGuardKeyFile(BucketService bucketService, BucketDirectory keyStoreDirectory, BucketDirectory documentDirectory) {
        DocumentKeyID documentKeyID = tryToLoadBucketGuardKeyFile(bucketService, keyStoreDirectory, documentDirectory);
        if (documentKeyID == null) {
            throw new NoDocumentGuardExists(keyStoreDirectory.append(documentDirectory.addSuffix(BUCKET_GUARD_KEY)));
        }
        return documentKeyID;
    }

    public static BucketDirectory getUniversalGuardDirecgtory(UserID userID) {
        return UserIDUtil.getHomeBucketDirectory(userID);
    }

}
