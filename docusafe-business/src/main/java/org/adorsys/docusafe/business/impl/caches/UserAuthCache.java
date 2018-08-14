package org.adorsys.docusafe.business.impl.caches;

import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.encobject.domain.ReadKeyPassword;

/**
 * Created by peter on 26.06.18 at 18:07.
 */
public interface UserAuthCache extends DocusafeCacheTemplate<UserID, ReadKeyPassword> {
}
