package org.adorsys.documentsafe.layer02service.impl.guardHelper;

import org.adorsys.documentsafe.layer02service.types.GuardKeyID;
import org.adorsys.encobject.keysource.KeySource;

/**
 * Created by peter on 16.02.18 at 17:38.
 */
public class KeySourceAndGuardKeyID {
    final public KeySource keySource;
    final public GuardKeyID guardKeyID;

    public KeySourceAndGuardKeyID(KeySource keySource, GuardKeyID guardKeyID) {
        this.keySource = keySource;
        this.guardKeyID = guardKeyID;
    }
}
