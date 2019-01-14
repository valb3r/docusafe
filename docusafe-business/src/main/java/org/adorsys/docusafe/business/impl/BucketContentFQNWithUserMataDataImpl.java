package org.adorsys.docusafe.business.impl;

import org.adorsys.docusafe.business.types.complex.BucketContentFQNWithUserMetaData;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.encobject.domain.UserMetaData;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by peter on 14.01.19 12:04.
 */
public class BucketContentFQNWithUserMataDataImpl extends BucketContentFQNImpl implements BucketContentFQNWithUserMetaData {
    private Map<DocumentFQN, UserMetaData> map = new HashMap<>();

    public void put(DocumentFQN documentFQN, UserMetaData userMetaData) {
        map.put(documentFQN, userMetaData);
    }

    @Override
    public UserMetaData getUserMetaData(DocumentFQN documentFQN) {
        return map.get(documentFQN);
    }
}
