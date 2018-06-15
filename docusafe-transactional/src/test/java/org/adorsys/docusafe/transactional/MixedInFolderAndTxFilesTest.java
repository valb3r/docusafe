package org.adorsys.docusafe.transactional;

import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.docusafe.business.impl.DocumentSafeServiceImpl;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.transactional.impl.TransactionalFileStorageImpl;
import org.adorsys.docusafe.transactional.types.TxID;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Security;

/**
 * Created by peter on 14.06.18 at 23:33.
 *
 * Dieser Test zeigt das Zusammenspiel vom Systembenutzer, der Dateien ausserhalb einer Transaktion
 * für einen Benutzer in einem "INBOX" Folder ablegt.
 * Dann greift der Benutzer innerhalb einer Transaktion auf diese Datei zu.
 *
 */
public class MixedInFolderAndTxFilesTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(MixedInFolderAndTxFilesTest.class);
    TransactionalFileStorage transactionalFileStorage = new TransactionalFileStorageImpl(new DocumentSafeServiceImpl(ExtendedStoreConnectionFactory.get()));
    UserIDAuth userIDAuth = new UserIDAuth(new UserID("peter"), new ReadKeyPassword("password"));
    UserIDAuth systemUserIDAuth = new UserIDAuth(new UserID("system"), new ReadKeyPassword("sys"));
    DocumentDirectoryFQN internalFolder = new DocumentDirectoryFQN("internalFolder");

    @Before
    public void preTest() {
        Security.addProvider(new BouncyCastleProvider());
        if (transactionalFileStorage.userExists(userIDAuth.getUserID())) {
            transactionalFileStorage.destroyUser(userIDAuth);
        }
        if (transactionalFileStorage.userExists(systemUserIDAuth.getUserID())) {
            transactionalFileStorage.destroyUser(systemUserIDAuth);
        }
    }

    public void a() {
        // System User anlegen
        transactionalFileStorage.createUser(systemUserIDAuth);

        // personalisierten Benutzer anlegen
        transactionalFileStorage.createUser(userIDAuth);
        // dem Systembenutzer Zugriff auf die die Inbox gewähren
        transactionalFileStorage.grantAccess(userIDAuth, systemUserIDAuth.getUserID());


        // prüfen, ob neue Dateien in der Inbox sind
        checkForNewInFiles();

    }

    private void checkForNewInFiles() {
        // starten einen Transaktion, denn der folgende list Befehl kann sich auf beliebiege
        // Verzeichnisse beziehen. Um zu wissen, welche Dateien gültig sind, ist Wissen über die
        // Transaktion notwendig

        TxID txID = transactionalFileStorage.beginTransaction(userIDAuth);

        BucketContentFQN list = transactionalFileStorage.listDocuments(userIDAuth, new DocumentDirectoryFQN("/"), ListRecursiveFlag.TRUE);
        list.getFiles().forEach(documentFQN -> {
            // Lade das Document TRANSAKTIONSLOS
            DSDocument dsDocument = transactionalFileStorage.readDocument(txID, userIDAuth, documentFQN);

            // Erzeuge ein neues Document mit einem anderen Pfad (nun internalFolder)
            DocumentFQN plainName = dsDocument.getDocumentFQN().getPlainNameWithoutPath();
            DSDocument internalDocument = new DSDocument(internalFolder.addName(plainName), dsDocument.getDocumentContent(), dsDocument.getDsDocumentMetaInfo());

            // Speichere das Document MIT TRANSAKTION
            transactionalFileStorage.storeDocument(txID, userIDAuth, internalDocument);
        });

        transactionalFileStorage.endTransaction(txID, userIDAuth);

        // Jetzt müssen im Nachgang die zuvor sicher kopierten Dateien gelöscht werden, damit sie nicht
        // erneut verarbeitet werden.
        list.getFiles().forEach(documentFQN -> {
            // Lade das Document TRANSAKTIONSLOS
           // transactionalFileStorage.deleteDocument();
        });

    }
}
