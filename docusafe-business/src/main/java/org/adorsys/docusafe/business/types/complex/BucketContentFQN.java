package org.adorsys.docusafe.business.types.complex;

import java.util.List;

/**
 * Created by peter on 14.06.18 at 11:32.
 */
public interface BucketContentFQN {
    List<DocumentFQN> getFiles();
    List<DocumentDirectoryFQN> getDirectories();
}
