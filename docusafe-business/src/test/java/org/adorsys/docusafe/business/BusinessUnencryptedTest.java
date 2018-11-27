package org.adorsys.docusafe.business;

import com.googlecode.catchexception.CatchException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DSDocumentStream;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.service.impl.UserMetaDataUtil;
import org.adorsys.docusafe.service.types.AccessType;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Created by peter on 20.06.18 at 10:04.
 */
@SuppressWarnings("Duplicates")
public class BusinessUnencryptedTest extends BusinessTestBase {
    private final static Logger LOGGER = LoggerFactory.getLogger(BusinessUnencryptedTest.class);

    @Before
    public void before() {
        super.before();
    }

    @Override
    public void after() {
        try {
            super.after();
        } finally {
        }
    }

    @Test
    public void createUAndDeleteUser() {
        {
            
            UserIDAuth userIDAuth = new UserIDAuth(new UserID("peter"), new ReadKeyPassword("peterkey"));
            service.createUser(userIDAuth);

            DocumentFQN documentFQN = new DocumentFQN("first/next/a-new-document.txt");
            DSDocumentMetaInfo mi = new DSDocumentMetaInfo();
            UserMetaDataUtil.setNoEncryption(mi);
            // try (DSDocumentStream dsDocument1 = createDocumentStream(userIDAuth, documentFQN, mi)) {
            try (InputStream is = createDocumentStream(userIDAuth, documentFQN, mi).getDocumentStream()) {

            } catch (Exception e) {
                throw BaseExceptionHandler.handle(e);
            }


            service.destroyUser(userIDAuth);
        }
    }


    @Test
    public void writeDocument() {
        
        UserIDAuth userIDAuth = createUser();
        UserIDAuth userIDAuthWrongPassword = new UserIDAuth(userIDAuth.getUserID(), new ReadKeyPassword("total falsch und anders"));
        Assert.assertEquals("Anzahl der guards", 1, getNumberOfGuards(userIDAuth.getUserID()));

        DocumentFQN documentFQN = new DocumentFQN("first/next/a-new-document.txt");
        checkGuardsForDocument(userIDAuth, documentFQN, false);
        DSDocumentMetaInfo mi = new DSDocumentMetaInfo();
        UserMetaDataUtil.setNoEncryption(mi);

        // Speichern mit falschen Kennwort nicht möglich, obwohl unverschluesselt
        CatchException.catchException(() -> createDocument(userIDAuthWrongPassword, documentFQN, mi));
        Assert.assertTrue(CatchException.caughtException() != null);

        // Speichern mit korrektem Kennwort
        DSDocument dsDocument1 = createDocument(userIDAuth, documentFQN, mi);
        checkGuardsForDocument(userIDAuth, documentFQN, true);
        Assert.assertEquals("Anzahl der guards", 2, getNumberOfGuards(userIDAuth.getUserID()));

        // Lesen mit falschen Kennwort nicht möglich, obwohl unverschluesselt
        CatchException.catchException(() -> readDocument(userIDAuthWrongPassword, documentFQN, dsDocument1.getDocumentContent(), true));
        Assert.assertTrue(CatchException.caughtException() != null);

        // Lesen mit korrektem Kennwort
        readDocument(userIDAuth, documentFQN, dsDocument1.getDocumentContent(), true);
    }

    @Test
    public void writeDocumentStream() {
        try {
            
            UserIDAuth userIDAuth = createUser();
            UserIDAuth userIDAuthWrongPassword = new UserIDAuth(userIDAuth.getUserID(), new ReadKeyPassword("total falsch und anders"));
            Assert.assertEquals("Anzahl der guards", 1, getNumberOfGuards(userIDAuth.getUserID()));

            DocumentFQN documentFQN = new DocumentFQN("first/next/a-new-document.txt");
            checkGuardsForDocument(userIDAuth, documentFQN, false);
            DSDocumentMetaInfo mi = new DSDocumentMetaInfo();
            UserMetaDataUtil.setNoEncryption(mi);
            boolean catched = false;

            {
                // Speichern mit falschen Kennwort nicht möglich, obwohl unverschluesselt
                CatchException.catchException(() -> createDocumentStream(userIDAuthWrongPassword, documentFQN, mi));
                Assert.assertTrue(CatchException.caughtException() != null);
            }

            // Speichern mit korrektem Kennwort
            DSDocumentStream dsDocument1 = createDocumentStream(userIDAuth, documentFQN, mi);
            checkGuardsForDocument(userIDAuth, documentFQN, true);
            Assert.assertEquals("Anzahl der guards", 2, getNumberOfGuards(userIDAuth.getUserID()));

            {
                boolean exceptionCaught = false;
                // Lesen mit falschen Kennwort nicht möglich, obwohl unverschluesselt
                try {
                    try (InputStream is = readDocumentStream(
                            userIDAuthWrongPassword,
                            documentFQN,
                            null, // stream kann nicht gebraucht werden, da test schon vorher auf Exception laufen muss
                            true).
                            getDocumentStream()) {
                    }
                    ;
                } catch(Exception e) {
                    exceptionCaught = true;
                }
                Assert.assertTrue(exceptionCaught);
            }

            // Lesen mit korrektem Kennwort
            try (InputStream is = dsDocument1.getDocumentStream()) {
                readDocumentStream(userIDAuth, documentFQN, is, true);
            }
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void writeGrantedDocument() {
        
        UserIDAuth userIDAuth1 = createUser(new UserID("user1"), new ReadKeyPassword("passwordOfUser1"));
        UserIDAuth userIDAuth1WrongPassword = new UserIDAuth(userIDAuth1.getUserID(), new ReadKeyPassword("total falsch und anders"));
        UserIDAuth userIDAuth2 = createUser(new UserID("user2"), new ReadKeyPassword("passwordOfUser2"));
        UserIDAuth userIDAuth2WrongPassword = new UserIDAuth(userIDAuth2.getUserID(), new ReadKeyPassword("wrong-password"));
        Assert.assertEquals("Anzahl der guards", 1, getNumberOfGuards(userIDAuth1.getUserID()));

        DocumentFQN documentFQN = new DocumentFQN("first/next/a-new-document.txt");
        service.grantAccessToUserForFolder(userIDAuth1, userIDAuth2.getUserID(), documentFQN.getDocumentDirectory(), AccessType.WRITE);

        DSDocumentMetaInfo mi = new DSDocumentMetaInfo();
        UserMetaDataUtil.setNoEncryption(mi);

        DocumentContent documentContent = new DocumentContent("Einfach nur a bisserl Text".getBytes());
        DSDocument dsDocument = new DSDocument(documentFQN, documentContent, mi);

        // Mit falschen Kennwort schreiben nicht möglich
        CatchException.catchException(() -> service.storeGrantedDocument(userIDAuth2WrongPassword, userIDAuth1.getUserID(), dsDocument));
        Assert.assertTrue(CatchException.caughtException() != null);

        service.grantAccessToUserForFolder(userIDAuth1, userIDAuth2.getUserID(), documentFQN.getDocumentDirectory(), AccessType.READ);
        // Mit richtigem Kennwort schreiben nun auch nicht möglich, da nur READ Berechtigung
        CatchException.catchException(() -> service.storeGrantedDocument(userIDAuth2, userIDAuth1.getUserID(), dsDocument));
        Assert.assertTrue(CatchException.caughtException() != null);

        service.grantAccessToUserForFolder(userIDAuth1, userIDAuth2.getUserID(), documentFQN.getDocumentDirectory(), AccessType.WRITE);
        service.storeGrantedDocument(userIDAuth2, userIDAuth1.getUserID(), dsDocument);

        // Lesen mit falschen Kennwort nicht möglich, obwohl unverschluesselt
        CatchException.catchException(() -> service.readGrantedDocument(userIDAuth2WrongPassword, userIDAuth1.getUserID(), documentFQN));
        Assert.assertTrue(CatchException.caughtException() != null);

        service.grantAccessToUserForFolder(userIDAuth1, userIDAuth2.getUserID(), documentFQN.getDocumentDirectory(), AccessType.NONE);
        // Lesen mit korrektem Kennwort nicht möglich, obwohl unverschluesselt, aber keine Berechtigung auf Verzeichnis
        CatchException.catchException(() -> service.readGrantedDocument(userIDAuth2, userIDAuth1.getUserID(), documentFQN));
        Assert.assertTrue(CatchException.caughtException() != null);

        service.grantAccessToUserForFolder(userIDAuth1, userIDAuth2.getUserID(), documentFQN.getDocumentDirectory(), AccessType.READ);

        // Lesen mit korrektem Kennwort
        service.readGrantedDocument(userIDAuth2, userIDAuth1.getUserID(), documentFQN);
    }

}
