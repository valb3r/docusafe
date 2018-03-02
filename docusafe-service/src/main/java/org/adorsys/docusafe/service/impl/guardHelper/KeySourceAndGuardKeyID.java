package org.adorsys.docusafe.service.impl.guardHelper;

import org.adorsys.docusafe.service.types.GuardKeyID;
import org.adorsys.encobject.service.api.KeySource;

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
