package org.adorsys.documentsafe.layer03business.utils;

import org.adorsys.documentsafe.layer01persistence.types.ListRecursiveFlag;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.KeyStoreBucketPath;
import org.adorsys.documentsafe.layer02service.BucketService;
import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
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
    private final static String BUCKET_TO_KEY_DELIMITER = "->";

    public static PlainFileName getHelperFilenameForGuardAndBucket(DocumentKeyID documentKeyID, BucketPath bucketPath) {
        return new PlainFileName(bucketPath.getObjectHandlePath() + BUCKET_TO_KEY_DELIMITER + documentKeyID.getValue());
    }


    /**
     * @return null oder key
     */
    public static DocumentKeyID findDocumentKeyID(BucketService bucketService, UserID userID, BucketPath bucketPath) {
        KeyStoreBucketPath keyStoreBucketPath = UserIDUtil.getKeyStoreBucketPath(userID);
        BucketContent bucketContent = bucketService.readDocumentBucket(keyStoreBucketPath, ListRecursiveFlag.TRUE);

        String prefix = bucketPath.getObjectHandlePath();
        LOGGER.debug("prefix " + prefix);
        for (StorageMetadata meta : bucketContent.getStrippedContent()) {
            LOGGER.debug("meta getname " + meta.getName());
            if (meta.getName().startsWith(prefix)) {
                String name = meta.getName();
                int i = name.indexOf(BUCKET_TO_KEY_DELIMITER);
                String key = name.substring(i + BUCKET_TO_KEY_DELIMITER.length());
                return new DocumentKeyID(key);
            }
        }
        return null;
    }

    /**
     * @return key
     */
    public static DocumentKeyID getDocumentKeyID(BucketService bucketService, UserID userID, BucketPath bucketPath) {
        DocumentKeyID documentKeyID = findDocumentKeyID(bucketService, userID, bucketPath);
        if (documentKeyID == null) {
            throw new GuardException("no guard found for bucket " + bucketPath);
        }
        return documentKeyID;
    }

}