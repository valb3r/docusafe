package org.adorsys.resource.server.service;

import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.security.auth.callback.CallbackHandler;

import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.service.KeystorePersistence;
import org.adorsys.encobject.service.UnknownContainerException;
import org.adorsys.jkeygen.keystore.PasswordCallbackUtils;
import org.adorsys.resource.server.basetypes.BucketName;
import org.adorsys.resource.server.basetypes.UserID;
import org.adorsys.resource.server.utils.KeyStoreHandleUtils;

import de.adorsys.resource.server.keyservice.KeyPairGenerator;
import de.adorsys.resource.server.keyservice.KeyStoreGenerator;
import de.adorsys.resource.server.keyservice.SecretKeyGenerator;

public class UserKeyStoreService {

	private KeystorePersistence keystorePersistence;
	SecretKeyGenerator secretKeyGenerator;

	public UserKeyStoreService(KeystorePersistence keystorePersistence) {
		super();
		this.keystorePersistence = keystorePersistence;
		secretKeyGenerator = new SecretKeyGenerator("AES", 256);
	}

	public void createUserKeyStore(UserID userId, CallbackHandler userKeystoreHandler, CallbackHandler keyPassHandler,
			BucketName bucketName) throws NoSuchAlgorithmException, CertificateException, UnknownContainerException {
		String keyStoreType = null;
		String serverKeyPairAliasPrefix = userId.getValue();
		Integer numberOfSignKeyPairs = 5;
		Integer numberOfEncKeyPairs = 5;
		Integer numberOfSecretKeys = 5;
		String keyStorePassword = userId.getValue();
		char[] password = PasswordCallbackUtils.getPassword(keyPassHandler, keyStorePassword);
		KeyPairGenerator encKeyPairGenerator = new KeyPairGenerator("RSA", 2048, "SHA256withRSA", "enc-"+userId.getValue());
		KeyPairGenerator signKeyPairGenerator = new KeyPairGenerator("RSA", 2048, "SHA256withRSA", "sign-"+userId.getValue());
		KeyStoreGenerator keyStoreGenerator = new KeyStoreGenerator(encKeyPairGenerator, signKeyPairGenerator,
				secretKeyGenerator, keyStoreType, serverKeyPairAliasPrefix, numberOfSignKeyPairs, numberOfEncKeyPairs,
				numberOfSecretKeys, new String(password));
		KeyStore userKeyStore = keyStoreGenerator.generate();
		ObjectHandle keystoreHandle = KeyStoreHandleUtils.userkeyStoreHandle(bucketName, userId);
		keystorePersistence.saveKeyStore(userKeyStore, userKeystoreHandler, keystoreHandle);
	}
}
