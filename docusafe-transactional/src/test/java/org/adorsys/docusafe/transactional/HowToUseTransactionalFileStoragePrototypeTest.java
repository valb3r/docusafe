package org.adorsys.docusafe.transactional;

import com.googlecode.catchexception.CatchException;
import org.adorsys.docusafe.business.exceptions.GuardException;
import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.docusafe.transactional.types.TxID;
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
// @SuppressWarnings("Duplicates")
public class HowToUseTransactionalFileStoragePrototypeTest extends TransactionFileStorageBaseTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(HowToUseTransactionalFileStoragePrototypeTest.class);

    @Test
    public void pseudoMain() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());

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

        // prüfen, ob neue Dateien in der Inbox sind
        // da noch nichts angelegt wurde, passiert hier erst einmal nichts
        int filesMoved = checkForNewInFiles();
        Assert.assertEquals(0, filesMoved);

        // stelle nun als system user eine Datei bereit
        transactionalFileStorage.nonTxStoreDocument(systemUserIDAuth, userIDAuth.getUserID(), newDocument(systemUserBaseDir.addName("file1")));

        // nun gibt es eine Datei, die wird innerhalb der Unterfunktion kopiert und dann gelöscht
        filesMoved = checkForNewInFiles();
        Assert.assertEquals(1, filesMoved);

        // jetzt liegt noch nichts neues an
        filesMoved = checkForNewInFiles();
        Assert.assertEquals(0, filesMoved);

        // Das Anlegen einer Datei in einem Unterverzeichnis der Inbox ist nicht gestattet, da der Grant immer nur
        // Verzeichnisweise gilt, nicht für Unterverzeichnisse
        CatchException.catchException(() -> transactionalFileStorage.nonTxStoreDocument(systemUserIDAuth, userIDAuth.getUserID(), newDocument(systemUserBaseDir.addDirectory("subdir").addName("file1"))));
        Assert.assertNotNull(CatchException.caughtException());

        // Wenn gewünscht, kann das granten hier erweitert werden. Dann könnte man verschiedenen
        // Benutzern die Zugriff auf verschiedene Unterverzeichnisse gewähren.

    }

    private DSDocument newDocument(DocumentFQN documentFQN) {
        return new DSDocument(documentFQN, new DocumentContent("some content".getBytes()), new DSDocumentMetaInfo());
    }

    // returns the number of files that have been imported
    private int checkForNewInFiles() {
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

        // Zu diesem Zeitpunkt enthält das Filesystem physisch foldgende Dateien:
        //
        // bp-peter/home/meta.tx/LastCommitedTxID.txt.zip
        // bp-peter/home/meta.tx/TransactionalHashMap.txt.3fa48f05-e00c-469a-a9e1-bfd4002fb23c.zip
        // bp-peter/home/nonttx/file1.zip
        // bp-peter/home/tx/file1.3fa48f05-e00c-469a-a9e1-bfd4002fb23c.zip

        // Jetzt müssen im Nachgang die zuvor sicher kopierten Dateien gelöscht werden, damit sie nicht
        // erneut verarbeitet werden.
        list.getFiles().forEach(documentFQN -> {
            transactionalFileStorage.nonTxDeleteDocument(userIDAuth, documentFQN);
        });

        return list.getFiles().size();
    }
}
