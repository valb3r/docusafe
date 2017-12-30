package org.adorsys.resource.server.basetypes;

import javax.crypto.SecretKey;

/**
 * Created by peter on 29.12.2017 at 13:55:33.
 * 
 * @fpo: we do not need to serialize this class.
 */
public class DocumentKey {
	
	SecretKey secretKey;
    
	public DocumentKey() {}

	public DocumentKey(SecretKey secretKey) {
		super();
		this.secretKey = secretKey;
	}

	public SecretKey getSecretKey() {
		return secretKey;
	}

	
}
