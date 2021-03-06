package org.adorsys.docusafe.service.impl.guardHelper;

import org.adorsys.docusafe.service.types.GuardKeyID;
import org.adorsys.docusafe.service.types.complextypes.DocumentKeyIDWithKey;
import org.adorsys.encobject.domain.KeyStoreAccess;
import org.adorsys.encobject.service.api.KeyStore2KeySourceHelper;
import org.adorsys.encobject.service.api.KeystorePersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 16.02.18 at 17:37.
 */
public class GuardKeyForPublicKeyHelper implements GuardKeyHelper{
    private final static Logger LOGGER = LoggerFactory.getLogger(GuardKeyForPublicKeyHelper.class);
    /**
     * holt sich aus dem KeyStore einen beliebigen PublicKey, mit dem der übergebene DocumentKey asymmetrisch veschlüsselt wird
     * Dort, wo der KeyStore liegt wird dann ein DocumentGuard erzeugt, der den verschlüsselten DocumentKey enthält.
     * Im Header des DocumentGuards steht die DocuemntKeyID.
     */
    public KeySourceAndGuardKeyID getKeySourceAndGuardKeyID(KeystorePersistence keystorePersistence,
                                                     KeyStoreAccess keyStoreAccess,
                                                     DocumentKeyIDWithKey documentKeyIDWithKey) {
        KeyStore2KeySourceHelper.KeySourceAndKeyID forPublicKey = KeyStore2KeySourceHelper.getForPublicKey(keystorePersistence, keyStoreAccess);
        GuardKeyID guardKeyID = new GuardKeyID(forPublicKey.getKeyID().getValue());
        LOGGER.debug("Guard created with asymmetric KeyID :" + guardKeyID);
        return new KeySourceAndGuardKeyID(forPublicKey.getKeySource(), guardKeyID);
    }
}
