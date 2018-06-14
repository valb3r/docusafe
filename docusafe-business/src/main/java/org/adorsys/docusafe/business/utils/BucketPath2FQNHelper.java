package org.adorsys.docusafe.business.utils;

import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;

/**
 * Created by peter on 14.06.18 at 11:41.
 */
public class BucketPath2FQNHelper {
    public static DocumentFQN path2FQN(BucketDirectory bucketDirectory, BucketPath bucketPath) {
        String bucketDirectoryString = bucketDirectory2String(bucketDirectory);
        String bucketPathString = bucketPath2String(bucketPath);
        if (bucketPathString.startsWith(bucketDirectoryString)) {
            return new DocumentFQN(bucketPathString.substring(bucketDirectoryString.length()));
        }
        throw new RuntimeException("Programming error. Expected " + bucketPathString + " to start with " + bucketDirectoryString);
    }

    public static DocumentDirectoryFQN directory2FQN(BucketDirectory bucketDirectory, BucketDirectory subdirectory) {
        String bucketDirectoryString = bucketDirectory2String(bucketDirectory);
        String subdirString = bucketDirectory2String(subdirectory);
        if (subdirString.startsWith(bucketDirectoryString)) {
            return new DocumentDirectoryFQN(subdirString.substring(bucketDirectoryString.length()));
        }
        throw new RuntimeException("Programming error. Expected " + subdirString + " to start with " + bucketDirectoryString);

    }

    private static String bucketPath2String(BucketPath bucketPath) {
        return bucketPath.getObjectHandle().getContainer() + BucketPath.BUCKET_SEPARATOR + bucketPath.getObjectHandle().getName();
    }

    private static String bucketDirectory2String(BucketDirectory bucketDirectory) {
        return bucketDirectory.getObjectHandle().getContainer() + BucketPath.BUCKET_SEPARATOR + bucketDirectory.getObjectHandle().getName();
    }
}
