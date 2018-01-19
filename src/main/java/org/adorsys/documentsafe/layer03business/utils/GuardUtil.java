package org.adorsys.documentsafe.layer03business.utils;

import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.adorsys.documentsafe.layer02service.types.DocumentKeyID;
import org.adorsys.documentsafe.layer02service.types.PlainFileName;
import org.adorsys.documentsafe.layer02service.types.complextypes.BucketContent;
import org.adorsys.documentsafe.layer03business.exceptions.GuardException;
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
        String p1 = bucketPath.getObjectHandlePath();
        String p2 = documentKeyID.getValue();
        String p3 = p1.replaceAll("/", "_");
        return new PlainFileName(p3 + BUCKET_TO_KEY_DELIMITER +p2);
    }

    public static DocumentKeyID getDocumentKeyID(BucketContent bucketContent, BucketPath bucketPath) {
        String p1 = bucketPath.getObjectHandlePath();
        String p3 = p1.replaceAll("/", "_");
        String p4 = bucketContent.getBucketPath().getSubBuckets();
        String prefix = p4 + p3;
        LOGGER.debug("prefix " + prefix);
        for (StorageMetadata meta : bucketContent.getContent()) {
            LOGGER.debug("meta getname " + meta.getName());
            if (meta.getName().startsWith(prefix)) {
                String name = meta.getName();
                int i = name.indexOf(BUCKET_TO_KEY_DELIMITER);
                String key = name.substring(i + BUCKET_TO_KEY_DELIMITER.length());
                return new DocumentKeyID(key);
            }
        }
        LOGGER.error(bucketContent.toString());
        throw new GuardException("no guard found for bucket " + bucketPath);
    }
}
