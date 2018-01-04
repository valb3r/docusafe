package org.adorsys.resource.server.persistence;

import java.security.KeyStore;

import javax.security.auth.callback.CallbackHandler;

import org.adorsys.encobject.service.BlobStoreConnection;
import org.adorsys.encobject.service.BlobStoreContextFactory;
import org.adorsys.jkeygen.keystore.KeyStoreService;
import org.adorsys.resource.server.exceptions.BaseException;
import org.adorsys.resource.server.exceptions.BaseExceptionHandler;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreName;
import org.adorsys.resource.server.persistence.basetypes.KeyStoreType;

/**
 * Service in charge of loading and storing user keys.
 * 
 * @author fpo
 *
 */
public class ExtendedKeystorePersistence {

	private BlobStoreConnection blobStoreConnection;

	public ExtendedKeystorePersistence(BlobStoreContextFactory blobStoreContextFactory) {
		this.blobStoreConnection = new BlobStoreConnection(blobStoreContextFactory);
	}

	public void saveKeyStore(KeyStore keystore, CallbackHandler storePassHandler, KeyStoreName keyStoreName) {
		try {
			// Match store type aggainst file extension
			if(!keyStoreName.getKeyStoreType().equals(new KeyStoreType(keystore.getType())))
					throw new BaseException("Invalid store type - expected : " + keystore.getType() + " but is: " + keyStoreName.getKeyStoreType().getValue());
			
			// write keystore to byte array.
			byte[] bs = KeyStoreService.toByteArray(keystore, keyStoreName.getValue(), storePassHandler);
			
			// write byte array to blob store.
			blobStoreConnection.putBlob(keyStoreName.toLocation() , bs);
		} catch (Exception e) {
			BaseExceptionHandler.handle(e);
		}
	}
	
	public KeyStore loadKeystore(KeyStoreName keyStoreName, CallbackHandler handler) {
		try {
			
			// Read bytes
			byte[] ksBytes = blobStoreConnection.getBlob(keyStoreName.toLocation());
			
			// Initialize key store
			return KeyStoreService.loadKeyStore(ksBytes, keyStoreName.getValue(), keyStoreName.getKeyStoreType().getValue(), handler);
		} catch (Exception e) {
			throw BaseExceptionHandler.handle(e);
		}
	}	
	

}
