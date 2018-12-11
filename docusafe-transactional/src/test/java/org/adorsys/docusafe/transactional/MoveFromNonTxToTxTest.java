package org.adorsys.docusafe.transactional;

import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 14.06.18 at 23:33.
 * <p>
 * Dieser Test zeigt das Zusammenspiel vom Systembenutzer, der Dateien ausserhalb einer Transaktion
 * für einen Benutzer in einem "INBOX" Folder ablegt.
 * Dann greift der Benutzer innerhalb einer Transaktion auf diese Datei zu.
 *
 * Wichtig ist, dass der documentSafeService nicht benutzt wird, denn das TransactionalDocumentSafeService Layer
 * setzt auf dem documentSafeService auf.
 */
@SuppressWarnings("Duplicates")
public class MoveFromNonTxToTxTest extends TransactionFileStorageBaseTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(MoveFromNonTxToTxTest.class);


    @Test
    public void pseudoMainExplicit() {
        DocumentDirectoryFQN systemUserBaseDir = init();

        // prüfen, ob neue Dateien in der Inbox sind
        // da noch nichts angelegt wurde, passiert hier erst einmal nichts
        Assert.assertEquals(0,checkForNewInFilesExplicit());

        // stelle nun als system user eine Datei bereit
        transactionalFileStorage.nonTxStoreDocument(systemUserIDAuth, userIDAuth.getUserID(), newDocument(systemUserBaseDir.addName("file1")));

        // nun gibt es eine Datei, die wird innerhalb der Unterfunktion kopiert und dann gelöscht
        Assert.assertEquals(1, checkForNewInFilesExplicit());
        Assert.assertEquals(0, checkForNewInFilesExplicit());
    }

    @Test
    public void pseudoMainImplicit() {
        DocumentDirectoryFQN systemUserBaseDir = init();

        // prüfen, ob neue Dateien in der Inbox sind
        // da noch nichts angelegt wurde, passiert hier erst einmal nichts
        Assert.assertEquals(0, checkForNewInFilesImplicit());

        // stelle nun als system user eine Datei bereit
        transactionalFileStorage.nonTxStoreDocument(systemUserIDAuth, userIDAuth.getUserID(), newDocument(systemUserBaseDir.addName("file1")));

        Assert.assertEquals(1, checkForNewInFilesImplicit());
        Assert.assertEquals(0, checkForNewInFilesImplicit());
  }


    private DocumentDirectoryFQN  init() {
        LOGGER.debug("create System User");
        transactionalFileStorage.createUser(systemUserIDAuth);
        DocumentDirectoryFQN systemUserBaseDir = new DocumentDirectoryFQN("systemuser");

        LOGGER.debug("create personal User");
        transactionalFileStorage.createUser(userIDAuth);
        // dem Systembenutzer Zugriff auf die die Inbox gewähren
        // der Name der Inbox ist festgelegt, kann nicht geändert werden, und ist fuer
        // alle Benutzer gleich. Jeder Benutzer hat nur genau eine inbox
        LOGGER.debug("grant system user access to non transactional folder of personal user");
        transactionalFileStorage.grantAccessToNonTxFolder(userIDAuth, systemUserIDAuth.getUserID(), systemUserBaseDir);
        return systemUserBaseDir;

    }

    private DSDocument newDocument(DocumentFQN documentFQN) {
        return new DSDocument(documentFQN, new DocumentContent("some content".getBytes()), new DSDocumentMetaInfo());
    }

    // returns the number of files that have been imported
    private int checkForNewInFilesImplicit() {
        LOGGER.debug("check for new files in non transactional folder");
        // txListDocuments (ohne txId) ist transaktionslos und bezieht sich damit immer
        // auf die inbox. Wenn man nur bestimmte Documente sehen möchte, dann man
        // das DocumentDirectoryFQN anpasen
        BucketContentFQN list = transactionalFileStorage.nonTxListDocuments(userIDAuth, new DocumentDirectoryFQN("/"), ListRecursiveFlag.TRUE);

        if (list.getFiles().isEmpty()) {
            LOGGER.debug("no new files found");
            // Nichts zu tuen, also return
            return 0;
        }

        // Es gibt also Dateien im der Inbox
        // wir verarbeiten alle Dokumente in einer Transaction
        LOGGER.debug("new files found");
        LOGGER.debug("start tx");
        transactionalFileStorage.beginTransaction(userIDAuth);
        list.getFiles().forEach(documentFQN -> {
            LOGGER.debug("load document " + documentFQN + " from non transactional folder");
            transactionalFileStorage.transferFromNonTxToTx(userIDAuth, documentFQN, documentFQN);
        });

        BucketContentFQN deepListBefore = dssi.list(userIDAuth, new DocumentDirectoryFQN("/"), ListRecursiveFlag.TRUE);
        deepListBefore.getFiles().forEach(file -> LOGGER.info(file.getValue()));
        deepListBefore.getFiles().contains(new DocumentFQN("/nonttx/systemuser/file1"));

        transactionalFileStorage.endTransaction(userIDAuth);

        BucketContentFQN deepListAfter = dssi.list(userIDAuth, new DocumentDirectoryFQN("/"), ListRecursiveFlag.TRUE);
        deepListAfter.getFiles().forEach(file -> LOGGER.info(file.getValue()));
        Assert.assertFalse(deepListAfter.getFiles().contains(new DocumentFQN("/nonttx/systemuser/file1")));

        return list.getFiles().size();
    }

    private int checkForNewInFilesExplicit() {
        LOGGER.debug("check for new files in non transactional folder");
        // txListDocuments (ohne txId) ist transaktionslos und bezieht sich damit immer
        // auf die inbox. Wenn man nur bestimmte Documente sehen möchte, dann man
        // das DocumentDirectoryFQN anpasen
        BucketContentFQN list = transactionalFileStorage.nonTxListDocuments(userIDAuth, new DocumentDirectoryFQN("/"), ListRecursiveFlag.TRUE);

        if (list.getFiles().isEmpty()) {
            LOGGER.debug("no new files found");
            // Nichts zu tuen, also return
            return 0;
        }

        // Es gibt also Dateien im der Inbox
        // wir verarbeiten alle Dokumente in einer Transaction
        LOGGER.debug("new files found");
        LOGGER.debug("start tx");
        transactionalFileStorage.beginTransaction(userIDAuth);
        list.getFiles().forEach(documentFQN -> {
            // Lade das Document TRANSAKTIONSLOS
            LOGGER.debug("load document " + documentFQN + " from non transactional folder");
            DSDocument dsDocument = transactionalFileStorage.nonTxReadDocument(userIDAuth, documentFQN);

            // Erzeuge ein neues Document
            // da hier mit einer Transaktion gearbeitet wird, wird das Dokument in eimem anderen
            // Namensraum abgelegt. Daher kann der Name identisch sein!

            // Speichere das Document MIT TRANSAKTION
            transactionalFileStorage.txStoreDocument(userIDAuth, dsDocument);
        });

        transactionalFileStorage.endTransaction(userIDAuth);

        BucketContentFQN deepList = dssi.list(userIDAuth, new DocumentDirectoryFQN("/"), ListRecursiveFlag.TRUE);
        deepList.getFiles().forEach(file -> LOGGER.info(file.getValue()));
        Assert.assertTrue(deepList.getFiles().contains(new DocumentFQN("/nonttx/systemuser/file1")));

        // Jetzt müssen im Nachgang die zuvor sicher kopierten Dateien gelöscht werden, damit sie nicht
        // erneut verarbeitet werden.
        list.getFiles().forEach(documentFQN -> {
            transactionalFileStorage.nonTxDeleteDocument(userIDAuth, documentFQN);
        });

        return list.getFiles().size();
    }
}
