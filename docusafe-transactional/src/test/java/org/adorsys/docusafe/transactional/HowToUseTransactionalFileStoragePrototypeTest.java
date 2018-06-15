package org.adorsys.docusafe.transactional;

import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 14.06.18 at 23:33.
 *
 * Dieser Test zeigt das Zusammenspiel vom Systembenutzer, der Dateien ausserhalb einer Transaktion
 * für einen Benutzer in einem "INBOX" Folder ablegt.
 * Dann greift der Benutzer innerhalb einer Transaktion auf diese Datei zu.
 *
 */
// @SuppressWarnings("Duplicates")
public class HowToUseTransactionalFileStoragePrototypeTest extends TransactionFileStorageBaseTest{

    private final static Logger LOGGER = LoggerFactory.getLogger(HowToUseTransactionalFileStoragePrototypeTest.class);

    @Test
    public void a() {
        LOGGER.info("create System User");
        transactionalFileStorage.createUser(systemUserIDAuth);

        LOGGER.info("create personal User");
        transactionalFileStorage.createUser(userIDAuth);
        // dem Systembenutzer Zugriff auf die die Inbox gewähren
        // der Name der Inbox ist festgelegt, kann nicht geändert werden, und ist fuer
        // alle Benutzer gleich. Jeder Benutzer hat nur genau eine inbox
        LOGGER.info("grant system user access to non transactional folder of personal user");
        transactionalFileStorage.grantAccess(userIDAuth, systemUserIDAuth.getUserID());

        // prüfen, ob neue Dateien in der Inbox sind
        // da noch nichts angelegt wurde, passiert hier erst einmal nichts
        checkForNewInFiles();

        // stelle nun als system user eine Datei bereit
        transactionalFileStorage.storeDocument(systemUserIDAuth, userIDAuth.getUserID(), newDocument("file1"));

        checkForNewInFiles();

    }

    private DSDocument newDocument(String filename) {
        return new DSDocument(new DocumentFQN(filename), new DocumentContent("some content".getBytes()), new DSDocumentMetaInfo());
    }

    private void checkForNewInFiles() {
        LOGGER.info("check for new files in non transactional folder");
        // listDocuments (ohne txId) ist transaktionslos und bezieht sich damit immer
        // auf die inbox. Wenn man nur bestimmte Documente sehen möchte, dann man
        // das DocumentDirectoryFQN anpasen
        BucketContentFQN list = transactionalFileStorage.listDocuments(userIDAuth, new DocumentDirectoryFQN("/"), ListRecursiveFlag.TRUE);

        if (list.getFiles().isEmpty()) {
            LOGGER.info("no new files found");
            // Nichts zu tuen, also return
            return;
        }

        // Es gibt also Dateien im der Inbox
        // wir verarbeiten alle Dokumente in einer Transaction
        LOGGER.info("new files found");
        LOGGER.info("start tx");
        TxID txID = transactionalFileStorage.beginTransaction(userIDAuth);
        list.getFiles().forEach(documentFQN -> {
            // Lade das Document TRANSAKTIONSLOS
            LOGGER.info("load document " + documentFQN + " from non transactional folder");
            DSDocument dsDocument = transactionalFileStorage.readDocument(userIDAuth, documentFQN);

            // Erzeuge ein neues Document
            // da hier mit einer Transaktion gearbeitet wird, wird das Dokument in eimem anderen
            // Namensraum abgelegt. Daher kann der Name identisch sein!

            // Speichere das Document MIT TRANSAKTION
            transactionalFileStorage.storeDocument(txID, userIDAuth, dsDocument);
        });

        transactionalFileStorage.endTransaction(txID, userIDAuth);

        // Zu diesem Zeitpunkt enthält das Filesystem physisch foldgende Dateien:
        //
        // bp-peter/home/meta.tx/LastCommitedTxID.txt.zip
        // bp-peter/home/meta.tx/TransactionalHashMap.txt.3fa48f05-e00c-469a-a9e1-bfd4002fb23c.zip
        // bp-peter/home/nonttx/file1.zip
        // bp-peter/home/tx/file1.3fa48f05-e00c-469a-a9e1-bfd4002fb23c.zip

        // Jetzt müssen im Nachgang die zuvor sicher kopierten Dateien gelöscht werden, damit sie nicht
        // erneut verarbeitet werden.
        list.getFiles().forEach(documentFQN -> {
            transactionalFileStorage.deleteDocument(userIDAuth, documentFQN);
        });
    }
}
