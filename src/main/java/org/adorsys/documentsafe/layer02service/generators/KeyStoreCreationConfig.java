package org.adorsys.documentsafe.layer02service.generators;

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

    public KeyPairGenerator getEncKeyPairGenerator(String keyPrefix) {
        return new KeyPairGenerator("RSA", 2048, "SHA256withRSA", "enc-" + keyPrefix);
    }

    public KeyPairGenerator getSignKeyPairGenerator(String keyPrefix) {
        return new KeyPairGenerator("RSA", 2048, "SHA256withRSA", "sign-" + keyPrefix);
    }

    public SecretKeyGenerator getSecretKeyGenerator(String keyPrefix) {
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
