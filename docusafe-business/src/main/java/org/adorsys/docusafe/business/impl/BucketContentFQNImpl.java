package org.adorsys.docusafe.business.impl;

import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 14.06.18 at 11:33.
 */
public class BucketContentFQNImpl implements BucketContentFQN {
    private List<DocumentFQN> files = new ArrayList<>();
    private List<DocumentDirectoryFQN> directories = new ArrayList();

    @Override
    public List<DocumentFQN> getFiles() {
        return files;
    }

    @Override
    public List<DocumentDirectoryFQN> getDirectories() {
        return directories;
    }
}
