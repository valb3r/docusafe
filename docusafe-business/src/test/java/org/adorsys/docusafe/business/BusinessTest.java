package org.adorsys.docusafe.business;

import com.googlecode.catchexception.CatchException;
import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.exceptions.NoWriteAccessException;
import org.adorsys.docusafe.business.exceptions.UserIDDoesNotExistException;
import org.adorsys.docusafe.business.exceptions.WrongPasswordException;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.BucketContentFQN;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.service.exceptions.NoDocumentGuardExists;
import org.adorsys.docusafe.service.types.AccessType;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.adorsys.encobject.types.ListRecursiveFlag;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 19.01.18 at 16:25.
 */
@SuppressWarnings("Duplicates")
public class BusinessTest extends BusinessTestBase {
    private final static Logger LOGGER = LoggerFactory.getLogger(BusinessTest.class);

    @Test
    public void documentExistsTest_DOC_36() {
        UserIDAuth userIDAuth = createUser(new UserID("UserPeter"), new ReadKeyPassword("peterkey"));
        DocumentFQN documentFQNReadme1 = new DocumentFQN("README.txt");
        DocumentFQN documentFQNReadme2 = new DocumentFQN("README2.txt");
        DocumentFQN documentFQNnewDir = new DocumentFQN("affe/README2.txt");
        Assert.assertTrue(service.documentExists(userIDAuth, documentFQNReadme1));
        Assert.assertFalse(service.documentExists(userIDAuth, documentFQNReadme2));
        Assert.assertFalse(service.documentExists(userIDAuth, documentFQNnewDir));
        UserIDAuth userIDAuth2 = new UserIDAuth(new UserID("UserPeter2"), new ReadKeyPassword("peterkey"));
        CatchException.catchException(() -> service.documentExists(userIDAuth2, documentFQNReadme1));
        Assert.assertNotNull(CatchException.caughtException());
        Assert.assertTrue(CatchException.caughtException() instanceof UserIDDoesNotExistException);

    }

    @Test
    public void performanceTest_DOC_29() {
        int REPEATS = 1;
        int i = 0;

        UserIDAuth userIDAuth = createUser();
        Assert.assertEquals("Anzahl der guards", 1, getNumberOfGuards(userIDAuth.getUserID()));


        while (i > 0) {
            LOGGER.info("wait for visualVM profiler " + i);
            try {
                Thread.currentThread().sleep(1000);
            } catch (Exception e) {
            }
            i--;
        }

        for (int j = 0; j < REPEATS; j++) {
            DocumentFQN documentFQN = new DocumentFQN("first/next/document" + j + ".txt");
            Assert.assertFalse(service.documentExists(userIDAuth, documentFQN));
            DocumentContent documentContent = new DocumentContent(("Einfach nur a bisserl Text" + j).getBytes());
            DSDocument dsDocument = new DSDocument(documentFQN, documentContent, new DSDocumentMetaInfo());
            service.storeDocument(userIDAuth, dsDocument);
            Assert.assertTrue(service.documentExists(userIDAuth, documentFQN));
            DSDocument dsDocumentResult = service.readDocument(userIDAuth, documentFQN);
            LOGGER.debug("original  document:" + new String(documentContent.getValue()));
            LOGGER.debug("retrieved document:" + new String(dsDocumentResult.getDocumentContent().getValue()));
            Assert.assertEquals("document content ok", documentContent, dsDocumentResult.getDocumentContent());
        }
    }

    @Test
    public void sequenceDiagramTest() {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID("user1"), new ReadKeyPassword("password1"));
        users.add(userIDAuth);
        service.createUser(userIDAuth);
    }

    @Test
    public void testCreateUser() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuth = createUser();
        Assert.assertEquals("Anzahl der guards muss 1 betragen", 1, getNumberOfGuards(userIDAuth.getUserID()));
    }


    @Test
    public void loadDSDocument() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuth = createUser(new UserID("affe"), new ReadKeyPassword("ab_irgendwas_cd"));
        DocumentFQN fqn = new DocumentFQN("README.txt");
        checkGuardsForDocument(userIDAuth, fqn, true);
        Assert.assertEquals("Anzahl der guards muss 1 betragen", 1, getNumberOfGuards(userIDAuth.getUserID()));
        // Dieser Read muss ok sein
        service.readDocument(userIDAuth, fqn);
        userIDAuth = new UserIDAuth(new UserID("affe"), new ReadKeyPassword("ab_123456789_cd"));
        Boolean catched = false;
        try {
            // Dieser Read muss fehlschlagen. Es gab einen Bug im Cache, wo statt des ReadKeyPassword der toString Text benutzt wurde.
            // Dieser ist aber mit **** ausgegraut, so dass alle Passworte bis auf Anfang und Ende gleich sind !!!
            service.readDocument(userIDAuth, fqn);
        } catch (BaseException e) {
            catched = true;
        }
        Assert.assertTrue(catched);
    }

    @Test
    public void deleteDocumentTest() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserID userID = new UserID("DelPeter");
        Assert.assertFalse(service.userExists(userID));
        UserIDAuth userIDAuth = createUser(userID);
        Assert.assertTrue(service.userExists(userID));
        DocumentFQN fqn = new DocumentFQN("README.txt");
        service.readDocument(userIDAuth, fqn);
        Assert.assertTrue(service.documentExists(userIDAuth, fqn));
        service.deleteDocument(userIDAuth, fqn);
        Assert.assertFalse(service.documentExists(userIDAuth, fqn));
        try {
            service.readDocument(userIDAuth, fqn);
        } catch (Exception e) {
            LOGGER.debug("Exception expected! Test is fine");
            return;
        }
        throw new BaseException("document is still readable:" + fqn);
    }

    @Test
    public void storeDSDocumentInANewFolder() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuth = createUser();
        Assert.assertEquals("Anzahl der guards", 1, getNumberOfGuards(userIDAuth.getUserID()));

        DocumentFQN documentFQN = new DocumentFQN("first/next/a-new-document.txt");
        checkGuardsForDocument(userIDAuth, documentFQN, false);
        DSDocument dsDocument1 = createDocument(userIDAuth, documentFQN);
        checkGuardsForDocument(userIDAuth, documentFQN, true);
        Assert.assertEquals("Anzahl der guards", 2, getNumberOfGuards(userIDAuth.getUserID()));
        readDocument(userIDAuth, documentFQN, dsDocument1.getDocumentContent());

        DSDocument dsDocument2 = createDocument(userIDAuth, new DocumentFQN("first/next/another-new-document.txt"));
        readDocument(userIDAuth, dsDocument2.getDocumentFQN(), dsDocument2.getDocumentContent());
        checkGuardsForDocument(userIDAuth, documentFQN, true);
        Assert.assertEquals("Anzahl der guards", 2, getNumberOfGuards(userIDAuth.getUserID()));
    }


    @Test
    public void grantAccessToFolder() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuthPeter = createUser(new UserID("peter"), new ReadKeyPassword("keyPasswordForPeter"));
        UserIDAuth userIDAuthFrancis = createUser(new UserID("francis"), new ReadKeyPassword("keyPasswordForFrancis"));
        DocumentFQN documentFQN = new DocumentFQN("first/document.txt");
        DSDocument dsDocument1 = createDocument(userIDAuthPeter, documentFQN);

        DocumentDirectoryFQN documentDirectoryFQN = new DocumentDirectoryFQN("first");

        service.grantAccessToUserForFolder(userIDAuthPeter, userIDAuthFrancis.getUserID(), documentDirectoryFQN, AccessType.WRITE);

        DSDocument dsDocument = service.readGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), documentFQN);
        Assert.assertEquals("document content ok", dsDocument1.getDocumentContent(), dsDocument.getDocumentContent());

        service.storeGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), dsDocument);
    }

    // Hier speichert Benuzter B etwas für Benutzer A und will es anschliessend lesen
    @Test
    public void grantReadAccessToFolder() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuthPeter = createUser(new UserID("peter"), new ReadKeyPassword("keyPasswordForPeter"));
        UserIDAuth userIDAuthFrancis = createUser(new UserID("francis"), new ReadKeyPassword("keyPasswordForFrancis"));
        DocumentFQN documentFQN = new DocumentFQN("first/next/a-new-document.txt");
        DSDocument dsDocument1 = createDocument(userIDAuthPeter, documentFQN);

        DocumentDirectoryFQN documentDirectoryFQN = new DocumentDirectoryFQN("first/next");

        service.grantAccessToUserForFolder(userIDAuthPeter, userIDAuthFrancis.getUserID(), documentDirectoryFQN, AccessType.WRITE);

        DSDocument dsDocument = service.readGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), documentFQN);
        Assert.assertEquals("document content ok", dsDocument1.getDocumentContent(), dsDocument.getDocumentContent());

        service.storeGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), dsDocument);
        // read again as francis
        service.readGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), documentFQN);
        // and read as peter
        service.readDocument(userIDAuthPeter, documentFQN);
        service.grantAccessToUserForFolder(userIDAuthPeter, userIDAuthFrancis.getUserID(), documentDirectoryFQN, AccessType.READ);
        boolean catched = false;
        try {
            service.storeGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), dsDocument);
        } catch (NoWriteAccessException e) {
            catched = true;
        }
        Assert.assertTrue(catched);
        service.readGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), documentFQN);

        service.grantAccessToUserForFolder(userIDAuthPeter, userIDAuthFrancis.getUserID(), documentDirectoryFQN, AccessType.NONE);
        catched = false;
        try {
            service.readGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), documentFQN);
        } catch (NoDocumentGuardExists e) {
            catched = true;
        }
        Assert.assertTrue(catched);

    }

    // Hier speichert Benuzter A etwas für Benutzer A (also sich selbst) und will es anschliessend Benutzer B lesen lassen
    @Test
    public void grantReadAccessToFolderForOwnDocuments() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuthPeter = createUser(new UserID("peter"), new ReadKeyPassword("keyPasswordForPeter"));
        UserIDAuth userIDAuthFrancis = createUser(new UserID("francis"), new ReadKeyPassword("keyPasswordForFrancis"));
        DocumentFQN documentFQN = new DocumentFQN("first/document.txt");
        DSDocument dsDocument1 = createDocument(userIDAuthPeter, documentFQN);

        CatchException.catchException(() -> service.readGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), documentFQN));
        Assert.assertNotNull(CatchException.caughtException());

        service.grantAccessToUserForFolder(userIDAuthPeter, userIDAuthFrancis.getUserID(), documentFQN.getDocumentDirectory(), AccessType.WRITE);
        service.readGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), documentFQN);
        service.readDocument(userIDAuthPeter, documentFQN);
    }


    /**
     * Zunächst erstellt peter ein document.
     * Dann darf Francis das Document auch lesen, aber nicht schreiben.
     * Dann darf er auch schreiben.
     * Schließlich darf er garnicht mehr drauf zugreifen.
     * Zuletzt wird gezeigt, dass Peter das Dokument nach wie vor schreiben darf.
     */
    @Test
    public void tryOverwriteGrantAccessToFolder() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuthPeter = createUser(new UserID("peter"), new ReadKeyPassword("keyPasswordForPeter"));
        UserIDAuth userIDAuthFrancis = createUser(new UserID("francis"), new ReadKeyPassword("keyPasswordForFrancis"));
        DocumentFQN documentFQN = new DocumentFQN("first/next/a-new-document.txt");
        DSDocument dsDocument = createDocument(userIDAuthPeter, documentFQN);

        DocumentDirectoryFQN documentDirectoryFQN = new DocumentDirectoryFQN("first/next");

        Assert.assertEquals("Anzahl der guards", 2, getNumberOfGuards(userIDAuthPeter.getUserID()));
        Assert.assertEquals("Anzahl der guards", 1, getNumberOfGuards(userIDAuthFrancis.getUserID()));

        service.grantAccessToUserForFolder(userIDAuthPeter, userIDAuthFrancis.getUserID(), documentDirectoryFQN, AccessType.READ);
        Assert.assertEquals("Anzahl der guards", 2, getNumberOfGuards(userIDAuthPeter.getUserID()));
        Assert.assertEquals("Anzahl der guards", 2, getNumberOfGuards(userIDAuthFrancis.getUserID()));
        boolean noWriteAccessExceptionCaught = false;
        try {
            service.storeGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), dsDocument);
        } catch (NoWriteAccessException e) {
            LOGGER.debug("NoWriteAccessException was expected. Now we give francis the write access");
            noWriteAccessExceptionCaught = true;

        }
        Assert.assertTrue(noWriteAccessExceptionCaught);
        service.grantAccessToUserForFolder(userIDAuthPeter, userIDAuthFrancis.getUserID(), documentDirectoryFQN, AccessType.WRITE);
        Assert.assertEquals("Anzahl der guards", 2, getNumberOfGuards(userIDAuthPeter.getUserID()));
        Assert.assertEquals("Anzahl der guards", 2, getNumberOfGuards(userIDAuthFrancis.getUserID()));

        service.storeGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), dsDocument);
        service.readGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), documentFQN);
        service.grantAccessToUserForFolder(userIDAuthPeter, userIDAuthFrancis.getUserID(), documentDirectoryFQN, AccessType.READ);
        service.readGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), documentFQN);

        service.grantAccessToUserForFolder(userIDAuthPeter, userIDAuthFrancis.getUserID(), documentDirectoryFQN, AccessType.NONE);
        Assert.assertEquals("Anzahl der guards", 2, getNumberOfGuards(userIDAuthPeter.getUserID()));
        Assert.assertEquals("Anzahl der guards", 1, getNumberOfGuards(userIDAuthFrancis.getUserID()));
        boolean documentGuardExists = true;
        try {
            service.readGrantedDocument(userIDAuthFrancis, userIDAuthPeter.getUserID(), documentFQN);
        } catch (NoDocumentGuardExists e) {
            documentGuardExists = false;
            LOGGER.debug("Exception was expected");
        }
        Assert.assertFalse("es darf kein dokument guard mehr exisitieren", documentGuardExists);

        DSDocument dsDocument1 = service.readDocument(userIDAuthPeter, documentFQN);
        service.storeDocument(userIDAuthPeter, dsDocument1);
    }

    @Test
    public void tryToDeleteUserWithWrongPassword() {
        UserIDAuth userIDAuthPeter = createUser(new UserID("peter"), new ReadKeyPassword("keyPasswordForPeter"));
        UserIDAuth wrongUserIDAuthPeter = new UserIDAuth(userIDAuthPeter.getUserID(), new ReadKeyPassword("WRONGPASSWORD"));
        boolean exceptionRaised = false;
        try {
            service.destroyUser(wrongUserIDAuthPeter);
        } catch (WrongPasswordException e) {
            LOGGER.debug("THIS EXCEPTION WAS EXPECTED");
            LOGGER.info("caught exception");
            exceptionRaised = true;
        }
        Assert.assertTrue(exceptionRaised);
        service.destroyUser(userIDAuthPeter);
        users.clear();

    }

    @Test
    public void checkDirectoryListings() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuth = createUser();
        DocumentDirectoryFQN dir = new DocumentDirectoryFQN("/");
        createDirectoryWithSubdirectories(3, userIDAuth, dir, 3, 3);
        {
            BucketContentFQN list = service.list(userIDAuth, dir, ListRecursiveFlag.FALSE);
            list.getDirectories().forEach(sdir -> LOGGER.debug("found dir " + sdir));
            list.getFiles().forEach(file -> LOGGER.debug("found file " + file));
            Assert.assertEquals(3, list.getDirectories().size());
            Assert.assertEquals(4, list.getFiles().size());
        }
        {
            BucketContentFQN list = service.list(userIDAuth, dir, ListRecursiveFlag.TRUE);
            list.getDirectories().forEach(sdir -> LOGGER.debug("found dir " + sdir));
            list.getFiles().forEach(file -> LOGGER.debug("found file " + file));
            Assert.assertEquals(12, list.getDirectories().size());
            Assert.assertEquals(40, list.getFiles().size());
        }
    }

    @Test
    public void checkRootDirectoryListings() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuth = createUser();
        DocumentDirectoryFQN dir = new DocumentDirectoryFQN("/");
        createDirectoryWithSubdirectories(2, userIDAuth, dir, 1, 1);
        {
            BucketContentFQN list = service.list(userIDAuth, dir, ListRecursiveFlag.FALSE);
            list.getDirectories().forEach(sdir -> LOGGER.debug("found dir " + sdir));
            list.getFiles().forEach(file -> LOGGER.debug("found file " + file));
            Assert.assertEquals(1, list.getDirectories().size());
            Assert.assertEquals(2, list.getFiles().size());
        }
        {
            BucketContentFQN list = service.list(userIDAuth, dir, ListRecursiveFlag.TRUE);
            list.getDirectories().forEach(sdir -> LOGGER.debug("found dir " + sdir));
            list.getFiles().forEach(file -> LOGGER.debug("found file " + file));
            Assert.assertEquals(1, list.getDirectories().size());
            Assert.assertEquals(3, list.getFiles().size());
        }
    }

    @Test
    public void checkRootDirectoryListingVerySimple() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuth = createUser();
        DocumentDirectoryFQN dir = new DocumentDirectoryFQN("/");
        DocumentFQN documentFQN = new DocumentFQN("/affe.txt");
        createDocument(userIDAuth, documentFQN);

        BucketContentFQN list = service.list(userIDAuth, dir, ListRecursiveFlag.FALSE);
        list.getDirectories().forEach(sdir -> LOGGER.debug("found dir " + sdir));
        list.getFiles().forEach(file -> LOGGER.debug("found file " + file));
        Assert.assertEquals(0, list.getDirectories().size());
        Assert.assertEquals(2, list.getFiles().size());

        list = service.list(userIDAuth, dir, ListRecursiveFlag.TRUE);
        list.getDirectories().forEach(sdir -> LOGGER.debug("found dir " + sdir));
        list.getFiles().forEach(file -> LOGGER.debug("found file " + file));
        Assert.assertEquals(0, list.getDirectories().size());
        Assert.assertEquals(2, list.getFiles().size());
    }

    @Test
    public void checkRootDirectoryListingSimple() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuth = createUser();
        DocumentDirectoryFQN dir = new DocumentDirectoryFQN("/anyfolder");
        DocumentFQN documentFQN = new DocumentFQN("/anyfolder/affe.txt");
        createDocument(userIDAuth, documentFQN);

        BucketContentFQN list = service.list(userIDAuth, dir, ListRecursiveFlag.FALSE);
        list.getDirectories().forEach(sdir -> LOGGER.debug("found dir " + sdir));
        list.getFiles().forEach(file -> LOGGER.debug("found file " + file));
        Assert.assertEquals(0, list.getDirectories().size());
        Assert.assertEquals(1, list.getFiles().size());

        list = service.list(userIDAuth, dir, ListRecursiveFlag.TRUE);
        list.getDirectories().forEach(sdir -> LOGGER.debug("found dir " + sdir));
        list.getFiles().forEach(file -> LOGGER.debug("found file " + file));
        Assert.assertEquals(0, list.getDirectories().size());
        Assert.assertEquals(1, list.getFiles().size());
    }


}
