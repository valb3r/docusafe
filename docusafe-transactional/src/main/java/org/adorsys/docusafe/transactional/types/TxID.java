package org.adorsys.docusafe.transactional.types;

import org.adorsys.cryptoutils.basetypes.BaseTypeString;

import java.util.UUID;

/**
 * Created by peter on 11.06.18 at 14:58.
 */
public class TxID extends BaseTypeString {
    public TxID() {
        this(UUID.randomUUID().toString());
    }
    public TxID(String txid) {
        super(txid);
    }
}
