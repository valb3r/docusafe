package org.adorsys.resource.server.complextypes;

import org.adorsys.resource.server.basetypes.DocumentGuardName;
import org.adorsys.resource.server.basetypes.EncryptedKey;
import org.adorsys.resource.server.basetypes.KeyID;

/**
 * Created by peter on 23.12.17 at 18:33.
 */
public class DocumentGuard {
    DocumentGuardName documentGuardName;

    KeyID key;
    EncryptedKey encryptedKey;
}
