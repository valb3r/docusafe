package org.adorsys.documentsafe.layer02service.impl.guardHelper;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.documentsafe.layer02service.impl.GuardKeyType;

/**
 * Created by peter on 16.02.18 at 17:44.
 */
public class GuardKeyHelperFactory {
    public static GuardKeyHelper getHelper(GuardKeyType guardKeyType) {
        switch(guardKeyType) {
            case PUBLIC_KEY :
                return new GuardKeyForPublicKeyHelper();
            case SECRET_KEY:
                return new GuardKeyForSecretKeyHelper();
            default:
                throw new BaseException("Missing switch for " + guardKeyType);
        }

    }

}
