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
        DSDocumentMetaInfo mi = new DSDocumentMetaInfo();
        UserMetaDataUtil.setNoEncryption(mi);

        // Speichern mit falschen Kennwort nicht möglich, obwohl unverschluesselt
        CatchException.catchException(() -> createDocument(userIDAuthWrongPassword, documentFQN, mi));
        Assert.assertTrue(CatchException.caughtException() != null);

        // Speichern mit korrektem Kennwort
        DSDocument dsDocument1 = createDocument(userIDAuth, documentFQN, mi);
        Assert.assertEquals("Anzahl der guards", 1, getNumberOfGuards(userIDAuth.getUserID()));

        // Lesen mit falschen Kennwort nicht möglich, obwohl unverschluesselt
        CatchException.catchException(() -> readDocument(userIDAuthWrongPassword, documentFQN, dsDocument1.getDocumentContent()));
        Assert.assertTrue(CatchException.caughtException() != null);

        // Lesen mit korrektem Kennwort
        readDocument(userIDAuth, documentFQN, dsDocument1.getDocumentContent());
    }

    @Test
    public void writeDocumentStream() {
        try {
            
            UserIDAuth userIDAuth = createUser();
            UserIDAuth userIDAuthWrongPassword = new UserIDAuth(userIDAuth.getUserID(), new ReadKeyPassword("total falsch und anders"));
            Assert.assertEquals("Anzahl der guards", 1, getNumberOfGuards(userIDAuth.getUserID()));

            DocumentFQN documentFQN = new DocumentFQN("first/next/a-new-document.txt");
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
            Assert.assertEquals("Anzahl der guards", 1, getNumberOfGuards(userIDAuth.getUserID()));

            {
                boolean exceptionCaught = false;
                // Lesen mit falschen Kennwort nicht möglich, obwohl unverschluesselt
                try {
                    try (InputStream is = readDocumentStream(
                            userIDAuthWrongPassword,
                            documentFQN,
                            null // stream kann nicht gebraucht werden, da test schon vorher auf Exception laufen muss
                            ).
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
                readDocumentStream(userIDAuth, documentFQN, is);
            }
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

}
