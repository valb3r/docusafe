package org.adorsys.docusafe.business;

import com.googlecode.catchexception.CatchException;
import lombok.extern.slf4j.Slf4j;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.*;
import org.adorsys.docusafe.service.impl.UserMetaDataUtil;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Created by peter on 20.06.18 at 10:04.
 */
@Slf4j
public class BusinessUnencryptedTest extends BusinessTestBase {

    @Test
    public void createUAndDeleteUser() {
        {
            
            UserIDAuth userIDAuth = new UserIDAuth(new UserID("peter"), new ReadKeyPassword("peterkey"));
            service.createUser(userIDAuth);

            DocumentFQN documentFQN = new DocumentFQN("first/next/a-new-document.txt");
            DSDocumentMetaInfo mi = new DSDocumentMetaInfo();
            UserMetaDataUtil.setNoEncryption(mi);
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
        DocumentFQN documentFQN = new DocumentFQN("first/next/a-new-document.txt");
        DSDocumentMetaInfo mi = new DSDocumentMetaInfo();
        UserMetaDataUtil.setNoEncryption(mi);

        // Speichern mit falschen Kennwort nicht möglich, obwohl unverschluesselt
        CatchException.catchException(() -> createDocument(userIDAuthWrongPassword, documentFQN, mi));
        Assert.assertTrue(CatchException.caughtException() != null);

        // Speichern mit korrektem Kennwort
        DSDocument dsDocument1 = createDocument(userIDAuth, documentFQN, mi);

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
            Assert.assertEquals("number of guards", 0, getNumberOfGuards(userIDAuth.getUserID()));

            DocumentFQN documentFQN = new DocumentFQN("first/next/a-new-document.txt");
            DSDocumentMetaInfo mi = new DSDocumentMetaInfo();
            UserMetaDataUtil.setNoEncryption(mi);
            boolean catched = false;

            {
                // storage must not be possible, because the wrong password is given, though the document is not encrypted at all
                CatchException.catchException(() -> createDocumentStream(userIDAuthWrongPassword, documentFQN, mi));
                Assert.assertTrue(CatchException.caughtException() != null);
            }

            // Speichern mit korrektem Kennwort
            DSDocumentStream dsDocumentStream = createDocumentStream(userIDAuth, documentFQN, mi);
            Assert.assertEquals("Anzahl der guards", 0, getNumberOfGuards(userIDAuth.getUserID()));

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
            try (InputStream is = dsDocumentStream.getDocumentStream()) {
                readDocumentStream(userIDAuth, documentFQN, is);

            }
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void saveAndReadStreamTest() throws Exception {
        UserIDAuth userIDAuth = createUser();

        DocumentFQN documentFQN = new DocumentFQN("VeryBigDocument.txt");
        DSDocumentMetaInfo mi = new DSDocumentMetaInfo();
        UserMetaDataUtil.setNoEncryption(mi);

        String content = "test stream content";
        try (ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes())) {
            DSDocumentStream dsDocumentStream = new DSDocumentStream(documentFQN, bis, mi);

            service.storeDocumentStream(userIDAuth, dsDocumentStream);
            log.info("successfully stored stream content: " + content);
        }

        String readContent;
        DSDocumentStream dsReadDocumentStream = service.readDocumentStream(userIDAuth, documentFQN);
        try (InputStream is = dsReadDocumentStream.getDocumentStream()) {
            readContent = IOUtils.toString(is, Charset.defaultCharset());
            log.info("successfully read stream content: " + readContent);
        }

        Assert.assertEquals(content, readContent);
    }
}
