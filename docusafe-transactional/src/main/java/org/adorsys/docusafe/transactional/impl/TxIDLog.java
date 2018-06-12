package org.adorsys.docusafe.transactional.impl;

import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.transactional.types.TxID;

import java.nio.charset.Charset;

/**
 * Created by peter on 11.06.18 at 15:48.
 */
public class TxIDLog {
    private static String LOG_FILE_NAME = "LastCommitedTxID.txt";
    private static DocumentFQN txidLogFilename = FileStorageImpl.txdir.addName(LOG_FILE_NAME);

    public static LastCommitedTxID findLastCommitedTxID(DocumentSafeService documentSafeService, UserIDAuth userIDAuth) {
        if (documentSafeService.documentExists(userIDAuth, txidLogFilename)) {
            DSDocument dsDocument = documentSafeService.readDocument(userIDAuth, txidLogFilename);
            return new LastCommitedTxID(new String(dsDocument.getDocumentContent().getValue(), Charset.forName("UTF8")));

        }
        return null;
    }
}
