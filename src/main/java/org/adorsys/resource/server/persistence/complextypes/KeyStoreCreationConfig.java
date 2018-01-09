package org.adorsys.resource.server.persistence.complextypes;

import de.adorsys.resource.server.keyservice.KeyPairGenerator;
import de.adorsys.resource.server.keyservice.SecretKeyGenerator;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreID;

/**
 * Created by peter on 09.01.18.
 */
public class KeyStoreCreationConfig {
    private final KeyPairGenerator encKeyPairGenerator;
    private final KeyPairGenerator signKeyPairGenerator;
    private final SecretKeyGenerator secretKeyGenerator;

    private final Integer encKeyNumber;
    private final Integer signKeyNumber;
    private final Integer secretKeyNumber;

    public KeyStoreCreationConfig(Integer encKeyPairNumber, Integer signKeyNumber, Integer secretKeyNumber, KeyStoreID keyStoreID) {
        this.encKeyNumber = encKeyPairNumber;
        this.signKeyNumber = signKeyNumber;
        this.secretKeyNumber = secretKeyNumber;

        secretKeyGenerator = new SecretKeyGenerator("AES", 256);
        encKeyPairGenerator = new KeyPairGenerator("RSA", 2048, "SHA256withRSA", "enc-" + keyStoreID.getValue());
        signKeyPairGenerator = new KeyPairGenerator("RSA", 2048, "SHA256withRSA", "sign-" + keyStoreID.getValue());
    }

    public KeyPairGenerator getEncKeyPairGenerator() {
        return encKeyPairGenerator;
    }

    public KeyPairGenerator getSignKeyPairGenerator() {
        return signKeyPairGenerator;
    }

    public SecretKeyGenerator getSecretKeyGenerator() {
        return secretKeyGenerator;
    }

    public Integer getEncKeyNumber() {
        return encKeyNumber;
    }

    public Integer getSignKeyNumber() {
        return signKeyNumber;
    }

    public Integer getSecretKeyNumber() {
        return secretKeyNumber;
    }
}
