package org.adorsys.documentsafe.layer02service.types;

import org.adorsys.cryptoutils.basetypes.BaseTypePasswordString;
import org.adorsys.cryptoutils.basetypes.BaseTypeString;

/**
 * Created by peter on 09.01.18 at 08:05.
 */
public class ReadKeyPassword extends BaseTypePasswordString {

    public ReadKeyPassword(String readKeyPassword) {
        super(readKeyPassword);
    }
}
