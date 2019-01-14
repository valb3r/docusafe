package org.adorsys.docusafe.business.types.complex;

import org.adorsys.encobject.domain.UserMetaData;

/**
 * Created by peter on 14.01.19 11:41.
 */
public interface BucketContentFQNWithUserMetaData extends BucketContentFQN {
    UserMetaData getUserMetaData(DocumentFQN documentFQN);
}
