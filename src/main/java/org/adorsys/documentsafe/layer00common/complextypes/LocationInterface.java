package org.adorsys.documentsafe.layer00common.complextypes;

import org.adorsys.encobject.domain.ObjectHandle;

/**
 * Created by peter on 06.01.18.
 */
public interface LocationInterface {
    ObjectHandle getLocationHandle();

    /*
    *
    * Es felht noch eine to and fromString mit ca. folgender Funktionalität. Wird aber noch nicht benötigt.
    * keyStoreBucketName = new KeyStoreBucketName(StringUtils.substringAfterLast(storeFQN, KeyStoreBucketName.BUCKET_SEPARATOR));
	* String storeName = StringUtils.substringBeforeLast(storeFQN, KeyStoreBucketName.BUCKET_SEPARATOR);
	* keyStoreType = new KeyStoreType(StringUtils.substringAfterLast(storeName, FILE_EXTENSION_SEPARATOR));
	* keyStoreID = new KeyStoreID(StringUtils.substringBeforeLast(storeName, FILE_EXTENSION_SEPARATOR));
    *
    */
}
