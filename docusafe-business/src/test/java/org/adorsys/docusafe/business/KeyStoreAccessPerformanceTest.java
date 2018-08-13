package org.adorsys.docusafe.business;

import com.googlecode.catchexception.CatchException;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentMetaInfo;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 12.07.18 at 13:14.
 */
public class KeyStoreAccessPerformanceTest extends BusinessTestBase {
    private final static Logger LOGGER = LoggerFactory.getLogger(KeyStoreAccessPerformanceTest.class);

    @Test
    public void a() {

        int MAX_DOCS = 100;
        UserIDAuth userIDAuth = new UserIDAuth(new UserID("peter"), new ReadKeyPassword("readKeyPassword"));
        service.createUser(userIDAuth);

        // LOGGER.debug("==================================================================");
        // CatchException.catchException(() -> Thread.currentThread().sleep(30000));
        StopWatch st = new StopWatch();
        st.start();

        for (int i = 0; i<MAX_DOCS; i++) {
            DocumentFQN documentFQN = new DocumentFQN("file-" + i);
            DocumentContent documentContent = new DocumentContent("affe".getBytes());
            DSDocument document = new DSDocument(documentFQN, documentContent, new DSDocumentMetaInfo());
            service.storeDocument(userIDAuth, document);
            service.readDocument(userIDAuth, document.getDocumentFQN());
        }

        st.stop();
        LOGGER.debug("time to store and read " + MAX_DOCS + " documents was " + st.toString());
    }
}
