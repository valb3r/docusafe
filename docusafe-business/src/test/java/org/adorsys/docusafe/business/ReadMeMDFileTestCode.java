package org.adorsys.docusafe.business;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.docusafe.business.impl.DocumentSafeServiceImpl;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.adorsys.encobject.domain.ReadKeyPassword;

import java.util.Arrays;

/**
 * Created by peter on 19.02.19 09:45.
 */
public class ReadMeMDFileTestCode {
    public static void main(String[] args) {
        // create service
        DocumentSafeService documentSafeService = new DocumentSafeServiceImpl(ExtendedStoreConnectionFactory.get());

        // create user
        UserIDAuth userIDAuth = new UserIDAuth(new UserID("peter"), new ReadKeyPassword("passwordOfPeter"));
        documentSafeService.createUser(userIDAuth);

        // create document
        DocumentFQN documentFQN = new DocumentFQN("first/document.txt");
        DocumentContent documentContent = new DocumentContent(("programming is the mirror of your mind").getBytes());
        DSDocument dsDocument = new DSDocument(documentFQN, documentContent, null);
        documentSafeService.storeDocument(userIDAuth, dsDocument);

        // read the document again
        DSDocument dsDocumentRead = documentSafeService.readDocument(userIDAuth, documentFQN);
        if (Arrays.equals(dsDocument.getDocumentContent().getValue(), dsDocumentRead.getDocumentContent().getValue()) == true) {
            System.out.println("read the following content from " + documentFQN + ":" + new String(dsDocumentRead.getDocumentContent().getValue()));
        } else {
            throw new BaseException("This will never happen :-)");
        }
    }
}
