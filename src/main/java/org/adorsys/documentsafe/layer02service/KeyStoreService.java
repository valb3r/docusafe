package org.adorsys.documentsafe.layer02service;

import org.adorsys.documentsafe.layer01persistence.types.KeyStoreID;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.KeyStoreDirectory;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.KeyStoreLocation;
import org.adorsys.documentsafe.layer02service.generators.KeyStoreCreationConfig;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAuth;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;

/**
 * Created by peter on 11.01.18.
 */
public interface KeyStoreService {
    KeyStoreLocation createKeyStore(KeyStoreID keyStoreID,
                                    KeyStoreAuth keyStoreAuth,
                                    KeyStoreDirectory keyStoreDirectory,
                                    KeyStoreCreationConfig config);

    KeyStore loadKeystore(KeyStoreLocation keyStoreLocation,
                          CallbackHandler userKeystoreHandler);
}
