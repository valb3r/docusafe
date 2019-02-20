package org.adorsys.docusafe.business.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.GrantAccessList;
import org.adorsys.docusafe.service.BucketService;
import org.adorsys.docusafe.service.types.PlainFileContent;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 29.01.18 at 17:51.
 */
public class GrantUtil {
    private final static Logger LOGGER = LoggerFactory.getLogger(GrantUtil.class);
    public final static String GRANT_EXT = ".grants";

    public static void saveBucketGrantFile(BucketService bucketService, BucketDirectory documentDirectory, UserID owner, UserID receiver, boolean grantOrRevoke) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        BucketPath grantFile = UserIDUtil.getGrantBucketDirectory(owner).append(documentDirectory.addSuffix(GRANT_EXT));
        GrantAccessList grantAccessList = new GrantAccessList();
        if (bucketService.fileExists(grantFile)) {
            PlainFileContent plainFileContent = bucketService.readPlainFile(grantFile);
            String gsonString = new String(plainFileContent.getValue());
            grantAccessList = gson.fromJson(gsonString, GrantAccessList.class);
        }

        if (grantOrRevoke) {
            grantAccessList.add(receiver);
        } else {
            grantAccessList.remove(receiver);
        }

        if (grantAccessList.isEmpty()) {
            bucketService.deletePlainFile(grantFile);
            return;
        }
        String gsonString = gson.toJson(grantAccessList);
        LOGGER.debug("write grant file contains " + gsonString);

        PlainFileContent plainFileContent = new PlainFileContent(gsonString.getBytes());
        bucketService.createPlainFile(grantFile, plainFileContent);
    }

    public static boolean existsAccess(BucketService bucketService, BucketDirectory documentDirectory, UserID owner, UserID receiver) {
        BucketPath grantFile = UserIDUtil.getGrantBucketDirectory(owner).append(documentDirectory.addSuffix(GRANT_EXT));
        if (!bucketService.fileExists(grantFile)) {
            return false;
        }

        PlainFileContent plainFileContent = bucketService.readPlainFile(grantFile);
        String gsonString = new String(plainFileContent.getValue());
        LOGGER.debug("read grant file contains " + gsonString);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        GrantAccessList grantAccessList = gson.fromJson(gsonString, GrantAccessList.class);
        if (grantAccessList.contains(receiver)) {
            return true;
        }
        return false;
    }
}
