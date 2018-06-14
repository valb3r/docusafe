package org.adorsys.docusafe.transactional.impl.helper;

import org.adorsys.docusafe.business.impl.BucketContentFQNImpl;
import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.types.ListRecursiveFlag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Created by peter on 14.06.18 at 17:54.
 */
public class BucketContentFromHashMapHelper {
    public static BucketContentFQN list(Set<DocumentFQN> keys, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        List<DocumentFQN> candidates = new ArrayList<>();
        keys.forEach(documentFQN -> {
            if (documentFQN.getValue().startsWith(documentDirectoryFQN.getValue())) {
                candidates.add(documentFQN);
            }
        });

        // finden aller Verzeichnisse
        // search:    /a/
        // candidate: /a/b/c/file1
        // result:    /a/b/
        //            /a/b/c

        Set<DocumentDirectoryFQN> dirCandidates = new HashSet<>();

        candidates.forEach(candidate -> {
            DocumentFQN remainder = new DocumentFQN(candidate.getValue().substring(documentDirectoryFQN.getValue().length()));
            // candidate /a/b/c/file1
            // search    /a
            // remainder   /b/c/file1

            String dirPath = remainder.getDocumentDirectory().getValue();
            // dirpath     /b/c
            StringTokenizer st = new StringTokenizer(dirPath, BucketPath.BUCKET_SEPARATOR);
            String dirbase = documentDirectoryFQN.getValue();
            if (dirbase.length() == 1) {
                dirbase = "";
            }
            while (st.hasMoreElements()) {
                dirbase = dirbase + BucketPath.BUCKET_SEPARATOR + st.nextToken();
                dirCandidates.add(new DocumentDirectoryFQN(dirbase));
                // fügt erst /a/b
                // dann      /a/b/c
                // ein
            }
        });

        if (recursiveFlag.equals(ListRecursiveFlag.TRUE)) {
            BucketContentFQN bucketContentFQN = new BucketContentFQNImpl();
            candidates.forEach(candidate -> bucketContentFQN.getFiles().add(candidate));
            dirCandidates.forEach(dirCandidate -> bucketContentFQN.getDirectories().add(dirCandidate));
            return bucketContentFQN;
        }

        // reduzieren
        //
        BucketContentFQN bucketContentFQN = new BucketContentFQNImpl();
        candidates.forEach(candidate -> {
            DocumentFQN remainder = new DocumentFQN(candidate.getValue().substring(documentDirectoryFQN.getValue().length()));
            // candidate /a/b/c/file1
            // search    /a
            // remainder   /b/c/file1
            if (remainder.getValue().lastIndexOf(BucketPath.BUCKET_SEPARATOR) == 0) {
                bucketContentFQN.getFiles().add(candidate);
            }
        });

        HashSet<DocumentDirectoryFQN> dirs = new HashSet<>();
        dirCandidates.forEach(dirCandidate -> {
            DocumentDirectoryFQN remainder = new DocumentDirectoryFQN(dirCandidate.getValue().substring(documentDirectoryFQN.getValue().length()));
            // dirCandidate /a/b/c
            // search       /a
            // remainder      /b/c
            if (remainder.getValue().lastIndexOf(BucketPath.BUCKET_SEPARATOR) == 0) {
                dirs.add(remainder);
            }
        });
        dirs.forEach(dir -> bucketContentFQN.getDirectories().add(dir));
        return bucketContentFQN;
    }
}
