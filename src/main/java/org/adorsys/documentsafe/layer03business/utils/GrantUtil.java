package org.adorsys.documentsafe.layer03business.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.adorsys.documentsafe.layer02service.BucketService;
import org.adorsys.documentsafe.layer02service.types.PlainFileContent;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentDirectory;
import org.adorsys.documentsafe.layer03business.types.AccessType;
import org.adorsys.documentsafe.layer03business.types.UserID;
import org.adorsys.documentsafe.layer03business.types.complex.GrantAccessList;

/**
 * Created by peter on 29.01.18 at 17:51.
 */
public class GrantUtil {
    private final static String GRANT_EXT = ".grants";
    public static void saveBucketGrantFile(BucketService bucketService, DocumentDirectory documentDirectory, UserID owner, UserID receiver, AccessType accessType) {
        BucketPath grantFile = UserIDUtil.getGrantBucketDirectory(owner).append(documentDirectory).add(GRANT_EXT);
        GrantAccessList grantAccessList = new GrantAccessList();
        if (bucketService.existsFile(grantFile)) {
            PlainFileContent plainFileContent = bucketService.readPlainFile(grantFile);
            Gson gson = new GsonBuilder().create();
            grantAccessList = gson.fromJson(new String(plainFileContent.getValue()), GrantAccessList.class);
        }

        grantAccessList.addOrReplace(receiver, accessType);
        Gson gson = new GsonBuilder().create();
        String jsonString = gson.toJson(grantAccessList);
        PlainFileContent plainFileContent = new PlainFileContent(jsonString.getBytes());
        bucketService.createPlainFile(grantFile, plainFileContent);
    }

}
