package org.adorsys.documentsafe.layer02service;

import org.adorsys.documentsafe.layer02service.generators.KeyStoreCreationConfig;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.KeyStoreAuth;
import org.adorsys.jkeygen.keystore.KeyStoreType;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;

/**
 * Created by peter on 11.01.18.
 */
public interface KeyStoreService {
    void createKeyStore(KeyStoreAuth keyStoreAuth,
                        KeyStoreType keyStoreType,
                        BucketPath keyStorePath,
                        KeyStoreCreationConfig config);

    KeyStore loadKeystore(BucketPath keyStorePath,
                          CallbackHandler userKeystoreHandler);
}
