package de.adorsys.resource.server.keyservice;

import javax.crypto.SecretKey;
import javax.security.auth.callback.CallbackHandler;

import org.adorsys.jkeygen.keystore.SecretKeyData;
import org.adorsys.jkeygen.secretkey.SecretKeyBuilder;

public class SecretKeyGenerator {

    private final String secretKeyAlgo;
    private final Integer keySize;

    public SecretKeyGenerator(String secretKeyAlgo, Integer keySize) {
        this.secretKeyAlgo = secretKeyAlgo;
        this.keySize = keySize;
    }

    public SecretKeyData generate(String alias, CallbackHandler secretKeyPassHandler) {
        SecretKey secretKey = new SecretKeyBuilder()
                .withKeyAlg(secretKeyAlgo)
                .withKeyLength(keySize)
                .build();

        return new SecretKeyData(secretKey, alias, secretKeyPassHandler);
    }
}
