package org.adorsys.docusafe.business;

import com.googlecode.catchexception.CatchException;
import org.adorsys.docusafe.business.types.MoveType;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.BucketContentFQNWithUserMetaData;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.adorsys.encobject.types.OverwriteFlag;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 21.02.19 13:54.
 */
public class InboxTest extends BusinessTestBase {
    private final static Logger LOGGER = LoggerFactory.getLogger(InboxTest.class);

    @Test
    public  void copyReadmeWithNewName() {
        UserIDAuth userIDAuthPeter = createUser(new UserID("UserPeter"), new ReadKeyPassword("peterkey"));
        UserIDAuth userIDAuthFrancis = createUser(new UserID("UserFrancis"), new ReadKeyPassword("franciskey"));
        DocumentFQN documentFQNReadme = new DocumentFQN("README.txt");
        DocumentFQN documentFQNReadmeCopiedToFrancis = new DocumentFQN("PetersREADME.txt");

        LOGGER.debug("Francis hat das Document noch nicht");
        CatchException.catchException(() -> service.readDocument(userIDAuthFrancis, documentFQNReadmeCopiedToFrancis));
        Assert.assertNotNull(CatchException.caughtException());

        LOGGER.debug("Peter gibt das Document an Francis");
        service.moveDocumnetToUser(userIDAuthPeter, userIDAuthFrancis.getUserID(), documentFQNReadme, documentFQNReadmeCopiedToFrancis, MoveType.KEEP_COPY);
        DSDocument document2 = service.readFromInbox(userIDAuthFrancis, documentFQNReadmeCopiedToFrancis, documentFQNReadmeCopiedToFrancis, OverwriteFlag.FALSE);

        LOGGER.debug("Das Document existiert nun auch bei Francis");
        DSDocument document3 = service.readDocument(userIDAuthFrancis, documentFQNReadmeCopiedToFrancis);

        LOGGER.debug("Das Document existiert auch bei Peter");
        DSDocument document4 = service.readDocument(userIDAuthPeter, documentFQNReadme);

        LOGGER.debug("Jetzt wird es aber wirklich verschoben");
        service.moveDocumnetToUser(userIDAuthPeter, userIDAuthFrancis.getUserID(), documentFQNReadme, documentFQNReadmeCopiedToFrancis, MoveType.MOVE);
        CatchException.catchException(() -> service.readDocument(userIDAuthPeter, documentFQNReadme));

        LOGGER.debug("Das Document existiert nun bei Peter nicht mehr, daher die Exception");
        Assert.assertNotNull(CatchException.caughtException());
    }

    @Test
    public  void copyReadmeWithSameName() {
        UserIDAuth userIDAuthPeter = createUser(new UserID("UserPeter"), new ReadKeyPassword("peterkey"));
        UserIDAuth userIDAuthFrancis = createUser(new UserID("UserFrancis"), new ReadKeyPassword("franciskey"));
        DocumentFQN documentFQNReadme = new DocumentFQN("README.txt");

        LOGGER.debug("Peter gibt ein Document an Francis");
        service.moveDocumnetToUser(userIDAuthPeter, userIDAuthFrancis.getUserID(), documentFQNReadme, documentFQNReadme, MoveType.MOVE);

        LOGGER.debug("Da Francis das Document schon hat, gibt es eine Exception");
        CatchException.catchException( () -> service.readFromInbox(userIDAuthFrancis, documentFQNReadme, documentFQNReadme, OverwriteFlag.FALSE));
        Assert.assertNotNull(CatchException.caughtException());

        LOGGER.debug("Da Francis das Document schon hat, forciert er die Kopie, dann gibt es keine Exception");
        service.readFromInbox(userIDAuthFrancis, documentFQNReadme, documentFQNReadme, OverwriteFlag.TRUE);
    }


    @Test
    public  void copyReadmeWithTwice() {
        UserIDAuth userIDAuthPeter = createUser(new UserID("UserPeter"), new ReadKeyPassword("peterkey"));
        UserIDAuth userIDAuthFrancis = createUser(new UserID("UserFrancis"), new ReadKeyPassword("franciskey"));
        DocumentFQN documentFQNReadme = new DocumentFQN("README.txt");

        LOGGER.debug("Peter gibt ein Document an Francis");
        service.moveDocumnetToUser(userIDAuthPeter, userIDAuthFrancis.getUserID(), documentFQNReadme, documentFQNReadme, MoveType.KEEP_COPY);
        LOGGER.debug("Peter gibt das  Document erneut an Francis, aber es wurde noch nicht abgeholt, daher Exception");
        CatchException.catchException( () -> service.moveDocumnetToUser(userIDAuthPeter, userIDAuthFrancis.getUserID(), documentFQNReadme, documentFQNReadme, MoveType.MOVE));
        Assert.assertNotNull(CatchException.caughtException());
    }

    @Test
    public  void testList() {
        UserIDAuth userIDAuthPeter = createUser(new UserID("UserPeter"), new ReadKeyPassword("peterkey"));
        UserIDAuth userIDAuthFrancis = createUser(new UserID("UserFrancis"), new ReadKeyPassword("franciskey"));
        DocumentFQN documentFQNReadme = new DocumentFQN("README.txt");
        DocumentFQN[] destinationFileList = {new DocumentFQN("/FILE1.txt"), new DocumentFQN("folder1/FILE1.txt"), new DocumentFQN("folder1/FILE2.txt"), new DocumentFQN("folder1/folder2/folder3/FILE1.txt")};

        LOGGER.debug("Peter gibt das selbe Document mehrfach an Francis");
        for (DocumentFQN doc : destinationFileList) {
            service.moveDocumnetToUser(userIDAuthPeter, userIDAuthFrancis.getUserID(), documentFQNReadme, doc, MoveType.KEEP_COPY);
        }

        BucketContentFQNWithUserMetaData bucketContentFQNWithUserMetaData = service.listInbox(userIDAuthFrancis);
        bucketContentFQNWithUserMetaData.getFiles().stream().forEach(file -> LOGGER.debug("found file in inbox:" + file));
        for (DocumentFQN doc : destinationFileList) {
            Assert.assertTrue(bucketContentFQNWithUserMetaData.getFiles().contains(doc));
        }
    }

}
