package org.adorsys.docusafe.business;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DSDocumentStream;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.service.types.AccessType;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public void writeDocument() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuth = createUser();
        UserIDAuth userIDAuthWrongPassword = new UserIDAuth(userIDAuth.getUserID(), new ReadKeyPassword("total falsch und anders"));
        Assert.assertEquals("Anzahl der guards", 1, getNumberOfGuards(userIDAuth.getUserID()));

        DocumentFQN documentFQN = new DocumentFQN("first/next/a-new-document.txt");
        checkGuardsForDocument(userIDAuth, documentFQN, false);
        DSDocumentMetaInfo mi = new DSDocumentMetaInfo();
        mi.setNoEncryption();
        boolean catched = false;
        try {
            // Speichern mit falschen Kennwort nicht möglich, obwohl unverschluesselt
            DSDocument dsDocument1 = createDocument(userIDAuthWrongPassword, documentFQN, mi);
        } catch (BaseException e) {
            catched = true;
        }
        Assert.assertTrue(catched);
        // Speichern mit korrektem Kennwort
        DSDocument dsDocument1 = createDocument(userIDAuth, documentFQN, mi);
        checkGuardsForDocument(userIDAuth, documentFQN, true);
        Assert.assertEquals("Anzahl der guards", 2, getNumberOfGuards(userIDAuth.getUserID()));

        catched = false;
        try {
            // Lesen mit falschen Kennwort nicht möglich, obwohl unverschluesselt
            readDocument(userIDAuthWrongPassword, documentFQN, dsDocument1.getDocumentContent(), true);
        } catch (BaseException e) {
            catched = true;
        }
        Assert.assertTrue(catched);
        // Lesen mit korrektem Kennwort
        readDocument(userIDAuth, documentFQN, dsDocument1.getDocumentContent(), true);
    }

    @Test
    public void writeDocumentStream() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuth = createUser();
        UserIDAuth userIDAuthWrongPassword = new UserIDAuth(userIDAuth.getUserID(), new ReadKeyPassword("total falsch und anders"));
        Assert.assertEquals("Anzahl der guards", 1, getNumberOfGuards(userIDAuth.getUserID()));

        DocumentFQN documentFQN = new DocumentFQN("first/next/a-new-document.txt");
        checkGuardsForDocument(userIDAuth, documentFQN, false);
        DSDocumentMetaInfo mi = new DSDocumentMetaInfo();
        mi.setNoEncryption();
        boolean catched = false;
        try {
            // Speichern mit falschen Kennwort nicht möglich, obwohl unverschluesselt
            DSDocumentStream dsDocument1 = createDocumentStream(userIDAuthWrongPassword, documentFQN, mi);
        } catch (BaseException e) {
            catched = true;
        }
        Assert.assertTrue(catched);
        // Speichern mit korrektem Kennwort
        DSDocumentStream dsDocument1 = createDocumentStream(userIDAuth, documentFQN, mi);
        checkGuardsForDocument(userIDAuth, documentFQN, true);
        Assert.assertEquals("Anzahl der guards", 2, getNumberOfGuards(userIDAuth.getUserID()));

        catched = false;
        try {
            // Lesen mit falschen Kennwort nicht möglich, obwohl unverschluesselt
            readDocumentStream(userIDAuthWrongPassword, documentFQN, dsDocument1.getDocumentStream(), true);
        } catch (BaseException e) {
            catched = true;
        }
        Assert.assertTrue(catched);
        // Lesen mit korrektem Kennwort
        readDocumentStream(userIDAuth, documentFQN, dsDocument1.getDocumentStream(), true);
    }

    @Test
    public void writeGrantedDocument() {
        LOGGER.debug("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        UserIDAuth userIDAuth1 = createUser(new UserID("user1"), new ReadKeyPassword("passwordOfUser1"));
        UserIDAuth userIDAuth1WrongPassword = new UserIDAuth(userIDAuth1.getUserID(), new ReadKeyPassword("total falsch und anders"));
        UserIDAuth userIDAuth2 = createUser(new UserID("user2"), new ReadKeyPassword("passwordOfUser2"));
        UserIDAuth userIDAuth2WrongPassword = new UserIDAuth(userIDAuth2.getUserID(), new ReadKeyPassword("wrong-password"));
        Assert.assertEquals("Anzahl der guards", 1, getNumberOfGuards(userIDAuth1.getUserID()));

        DocumentFQN documentFQN = new DocumentFQN("first/next/a-new-document.txt");
        service.grantAccessToUserForFolder(userIDAuth1, userIDAuth2.getUserID(), documentFQN.getDocumentDirectory(), AccessType.WRITE);

        DSDocumentMetaInfo mi = new DSDocumentMetaInfo();
        mi.setNoEncryption();

        DocumentContent documentContent = new DocumentContent("Einfach nur a bisserl Text".getBytes());
        DSDocument dsDocument = new DSDocument(documentFQN, documentContent, mi);

        boolean catched = false;
        try {
            // Mit falschen Kennwort schreiben nicht möglich
            service.storeGrantedDocument(userIDAuth2WrongPassword, userIDAuth1.getUserID(), dsDocument);
        } catch (BaseException e) {
            catched = true;
        }
        Assert.assertTrue(catched);
        service.grantAccessToUserForFolder(userIDAuth1, userIDAuth2.getUserID(), documentFQN.getDocumentDirectory(), AccessType.READ);
        try {
            // Mit richtigem Kennwort schreiben nun auch nicht möglich, da nur READ Berechtigung
            service.storeGrantedDocument(userIDAuth2, userIDAuth1.getUserID(), dsDocument);
        } catch (BaseException e) {
            catched = true;
        }
        Assert.assertTrue(catched);
        service.grantAccessToUserForFolder(userIDAuth1, userIDAuth2.getUserID(), documentFQN.getDocumentDirectory(), AccessType.WRITE);
        service.storeGrantedDocument(userIDAuth2, userIDAuth1.getUserID(), dsDocument);

        catched = false;
        try {
            // Lesen mit falschen Kennwort nicht möglich, obwohl unverschluesselt
            service.readGrantedDocument(userIDAuth2WrongPassword, userIDAuth1.getUserID(), documentFQN);
        } catch (BaseException e) {
            catched = true;
        }
        Assert.assertTrue(catched);

        service.grantAccessToUserForFolder(userIDAuth1, userIDAuth2.getUserID(), documentFQN.getDocumentDirectory(), AccessType.NONE);
        catched = false;
        try {
            // Lesen mit korrektem Kennwort nicht möglich, obwohl unverschluesselt, aber keine Berechtigung auf Verzeichnis
            service.readGrantedDocument(userIDAuth2, userIDAuth1.getUserID(), documentFQN);
        } catch (BaseException e) {
            catched = true;
        }
        Assert.assertTrue(catched);

        service.grantAccessToUserForFolder(userIDAuth1, userIDAuth2.getUserID(), documentFQN.getDocumentDirectory(), AccessType.READ);

        // Lesen mit korrektem Kennwort
        service.readGrantedDocument(userIDAuth2, userIDAuth1.getUserID(), documentFQN);
    }

}
