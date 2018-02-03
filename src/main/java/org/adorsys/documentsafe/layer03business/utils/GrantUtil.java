package org.adorsys.documentsafe.layer03business.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.adorsys.documentsafe.layer02service.BucketService;
import org.adorsys.documentsafe.layer02service.types.PlainFileContent;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentDirectory;
import org.adorsys.documentsafe.layer03business.types.AccessType;
import org.adorsys.documentsafe.layer03business.types.UserID;
import org.adorsys.documentsafe.layer03business.types.complex.GrantAccessList;
import org.adorsys.encobject.complextypes.BucketPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 29.01.18 at 17:51.
 */
public class GrantUtil {
    private final static Logger LOGGER = LoggerFactory.getLogger(GrantUtil.class);
    public final static String GRANT_EXT = ".grants";
    private final static Gson gson = new GsonBuilder().create();

    public static void saveBucketGrantFile(BucketService bucketService, DocumentDirectory documentDirectory, UserID owner, UserID receiver, AccessType accessType) {
        BucketPath grantFile = UserIDUtil.getGrantBucketDirectory(owner).append(documentDirectory).add(GRANT_EXT);
        GrantAccessList grantAccessList = new GrantAccessList();
        if (bucketService.existsFile(grantFile)) {
            PlainFileContent plainFileContent = bucketService.readPlainFile(grantFile);
            String gsonString = new String(plainFileContent.getValue());
            grantAccessList = gson.fromJson(gsonString, GrantAccessList.class);
        }

        grantAccessList.addOrReplace(receiver, accessType);

        if (grantAccessList.isEmpty()) {
            bucketService.deletePlainFile(grantFile);
            return;
        }
        String gsonString = gson.toJson(grantAccessList);

        PlainFileContent plainFileContent = new PlainFileContent(gsonString.getBytes());
        bucketService.createPlainFile(grantFile, plainFileContent);
    }

    public static AccessType getAccessTypeOfBucketGrantFile(BucketService bucketService, DocumentDirectory documentDirectory, UserID owner, UserID receiver) {
        BucketPath grantFile = UserIDUtil.getGrantBucketDirectory(owner).append(documentDirectory).add(GRANT_EXT);
        if (!bucketService.existsFile(grantFile)) {
            return AccessType.NONE;
        }

        PlainFileContent plainFileContent = bucketService.readPlainFile(grantFile);
        String gsonString = new String(plainFileContent.getValue());
        GrantAccessList grantAccessList = gson.fromJson(gsonString, GrantAccessList.class);
        if (grantAccessList.find(receiver) != null) {
            return grantAccessList.find(receiver);
        }
        return AccessType.NONE;
    }
}