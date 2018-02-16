package org.adorsys.documentsafe.layer02service.impl.guardHelper;

import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.PasswordLookup;
import com.nimbusds.jose.jwk.RSAKey;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.documentsafe.layer02service.exceptions.AsymmetricEncryptionException;
import org.adorsys.documentsafe.layer02service.keysource.KeyStoreBasedPublicKeySourceImpl;
import org.adorsys.documentsafe.layer02service.types.GuardKeyID;
import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentKeyIDWithKeyAndAccessType;
import org.adorsys.documentsafe.layer02service.types.complextypes.KeyStoreAccess;
import org.adorsys.encobject.keysource.KeySource;
import org.adorsys.encobject.service.KeystorePersistence;
import org.adorsys.jjwk.keystore.JwkExport;
import org.adorsys.jkeygen.utils.V3CertificateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

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
                                                     DocumentKeyIDWithKeyAndAccessType documentKeyIDWithKeyAndAccessType) {
        LOGGER.info("start create asymmetric encrypted document guard for " + documentKeyIDWithKeyAndAccessType + " at " + keyStoreAccess.getKeyStorePath());
        KeyStore userKeystore = keystorePersistence.loadKeystore(keyStoreAccess.getKeyStorePath().getObjectHandle(), keyStoreAccess.getKeyStoreAuth().getReadStoreHandler());

        JWKSet exportKeys = load(userKeystore, null);
        LOGGER.debug("number of public keys found:" + exportKeys.getKeys().size());
        List<JWK> encKeys = selectEncKeys(exportKeys);
        if (encKeys.isEmpty()) {
            throw new AsymmetricEncryptionException("did not find any public keys in keystore " + keyStoreAccess.getKeyStorePath());
        }
        JWK randomKey = JwkExport.randomKey(encKeys);
        GuardKeyID guardKeyID = new GuardKeyID(randomKey.getKeyID());
        LOGGER.debug("Guard created with asymmetric KeyID :" + guardKeyID);

        KeySource keySource = new KeyStoreBasedPublicKeySourceImpl(exportKeys);
        return new KeySourceAndGuardKeyID(keySource, guardKeyID);
    }


    private static List<JWK> selectEncKeys(JWKSet exportKeys) {
        JWKMatcher signKeys = (new JWKMatcher.Builder()).keyUse(KeyUse.ENCRYPTION).build();
        return (new JWKSelector(signKeys)).select(exportKeys);
    }

    private JWKSet load(final KeyStore keyStore, final PasswordLookup pwLookup) {
        try {

            List<JWK> jwks = new LinkedList<>();

            // Load RSA and EC keys
            for (Enumeration<String> keyAliases = keyStore.aliases(); keyAliases.hasMoreElements(); ) {

                final String keyAlias = keyAliases.nextElement();
                final char[] keyPassword = pwLookup == null ? "".toCharArray() : pwLookup.lookupPassword(keyAlias);

                Certificate cert = keyStore.getCertificate(keyAlias);
                if (cert == null) {
                    continue; // skip
                }

                Certificate[] certs = new Certificate[]{cert};
                if (cert.getPublicKey() instanceof RSAPublicKey) {
                    List<X509Certificate> convertedCert = V3CertificateUtils.convert(certs);
                    RSAKey rsaJWK = RSAKey.parse(convertedCert.get(0));

                    // Let keyID=alias
                    // Converting from a certificate, the id is set as the thumbprint of the certificate.
                    rsaJWK = new RSAKey.Builder(rsaJWK).keyID(keyAlias).keyStore(keyStore).build();
                    jwks.add(rsaJWK);

                } else if (cert.getPublicKey() instanceof ECPublicKey) {
                    List<X509Certificate> convertedCert = V3CertificateUtils.convert(certs);
                    ECKey ecJWK = ECKey.parse(convertedCert.get(0));

                    // Let keyID=alias
                    // Converting from a certificate, the id is set as the thumbprint of the certificate.
                    ecJWK = new ECKey.Builder(ecJWK).keyID(keyAlias).keyStore(keyStore).build();
                    jwks.add(ecJWK);
                } else {
                    continue;
                }
            }
            return new JWKSet(jwks);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }


}
