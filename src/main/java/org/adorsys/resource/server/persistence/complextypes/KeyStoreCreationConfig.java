package org.adorsys.resource.server.persistence.complextypes;

import de.adorsys.resource.server.keyservice.KeyPairGenerator;
import de.adorsys.resource.server.keyservice.SecretKeyGenerator;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreID;

/**
 * Created by peter on 09.01.18.
 */
public class KeyStoreCreationConfig {
    private final Integer encKeyNumber;
    private final Integer signKeyNumber;
    private final Integer secretKeyNumber;

    /**
     * Weitere Konstruktoren ggf. wenn Algorithmen angepasst werden sollen
     * @param encKeyNumber
     * @param signKeyNumber
     * @param secretKeyNumber
     */
    public KeyStoreCreationConfig(Integer encKeyNumber, Integer signKeyNumber, Integer secretKeyNumber) {
        this.encKeyNumber = encKeyNumber;
        this.signKeyNumber = signKeyNumber;
        this.secretKeyNumber = secretKeyNumber;
    }

    public KeyPairGenerator getEncKeyPairGenerator(KeyStoreID keyStoreID) {
        return new KeyPairGenerator("RSA", 2048, "SHA256withRSA", "enc-" + keyStoreID.getValue());
    }

    public KeyPairGenerator getSignKeyPairGenerator(KeyStoreID keyStoreID) {
        return new KeyPairGenerator("RSA", 2048, "SHA256withRSA", "sign-" + keyStoreID.getValue());
    }

    public SecretKeyGenerator getSecretKeyGenerator(KeyStoreID keyStoreID) {
        return new SecretKeyGenerator("AES", 256);
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
