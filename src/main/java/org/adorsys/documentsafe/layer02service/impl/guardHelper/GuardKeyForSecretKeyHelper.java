package org.adorsys.documentsafe.layer02service.impl.guardHelper;

import com.nimbusds.jose.jwk.JWKSet;
import org.adorsys.documentsafe.layer02service.exceptions.SymmetricEncryptionException;
import org.adorsys.documentsafe.layer02service.keysource.KeyStoreBasedSecretKeySourceImpl;
import org.adorsys.documentsafe.layer02service.types.GuardKeyID;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKeyAndAccessType;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.encobject.keysource.KeySource;
import org.adorsys.encobject.service.KeystorePersistence;
import org.adorsys.jjwk.keystore.JwkExport;
import org.adorsys.jjwk.serverkey.KeyAndJwk;
import org.adorsys.jjwk.serverkey.ServerKeyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyStore;

/**
 * Created by peter on 16.02.18 at 17:36.
 */
public class GuardKeyForSecretKeyHelper implements GuardKeyHelper{
    private final static Logger LOGGER = LoggerFactory.getLogger(GuardKeyForSecretKeyHelper.class);
    /**
     * holt sich aus dem KeyStore einen beliebigen SecretKey, mit dem der übergebene DocumentKey symmetrisch veschlüsselt wird
     * Dort, wo der KeyStore liegt wird dann ein DocumentGuard erzeugt, der den verschlüsselten DocumentKey enthält.
     * Im Header des DocumentGuards steht die DocuemntKeyID.
     */
    public KeySourceAndGuardKeyID getKeySourceAndGuardKeyID(KeystorePersistence keystorePersistence,
                                                            KeyStoreAccess keyStoreAccess,
                                                            DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType) {
        LOGGER.info("start create symmetric encrypted document guard for " + documentKeyIDWithKeyAndAccessType + " at " + keyStoreAccess.getKeyStorePath());
        // KeyStore laden
        KeyStore userKeystore = keystorePersistence.loadKeystore(keyStoreAccess.getKeyStorePath().getObjectHandle(), keyStoreAccess.getKeyStoreAuth().getReadStoreHandler());
        KeySource keySource = new KeyStoreBasedSecretKeySourceImpl(userKeystore, keyStoreAccess.getKeyStoreAuth().getReadKeyHandler());

        // Willkürlich einen SecretKey aus dem KeyStore nehmen für die Verschlüsselung des Guards
        JWKSet jwkSet = JwkExport.exportKeys(userKeystore, keyStoreAccess.getKeyStoreAuth().getReadKeyHandler());
        if (jwkSet.getKeys().isEmpty()) {
            throw new SymmetricEncryptionException("did not find any secret keys in keystore with id: " + keyStoreAccess.getKeyStorePath());
        }
        ServerKeyMap serverKeyMap = new ServerKeyMap(jwkSet);
        KeyAndJwk randomSecretKey = serverKeyMap.randomSecretKey();
        GuardKeyID guardKeyID = new GuardKeyID(randomSecretKey.jwk.getKeyID());
        LOGGER.debug("Guard created with symmetric KeyID :" + guardKeyID);
        return new KeySourceAndGuardKeyID(keySource, guardKeyID);
    }

}
