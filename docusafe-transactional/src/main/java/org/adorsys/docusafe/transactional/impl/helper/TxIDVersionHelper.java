package org.adorsys.docusafe.transactional.impl.helper;

import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.transactional.types.TxID;

/**
 * Created by peter on 12.06.18 at 08:03.
 */
public class TxIDVersionHelper {
    public static DocumentFQN get(DocumentFQN documentFQN, TxID txid) {
        return new DocumentFQN(documentFQN.getValue() + "." + txid.getValue());
    }
}
