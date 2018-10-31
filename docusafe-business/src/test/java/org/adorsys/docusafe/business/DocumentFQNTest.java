package org.adorsys.docusafe.business;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 15.06.18 at 14:17.
 */
public class DocumentFQNTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentFQNTest.class);

    @Test
    public void testValidName() {
        new DocumentFQN("/");
        new DocumentFQN("a");
        new DocumentFQN("");
        new DocumentFQN("/affe/a");
    }


    @Test (expected = BaseException.class)
    public void testNonValid1() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        new DocumentFQN("//");
    }

    @Test (expected = BaseException.class)
    public void testNonValid2() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        new DocumentFQN("a/");
    }

    @Test (expected = BaseException.class)
    public void testNonValid3() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        new DocumentFQN("a/b//c");
    }


    @Test
    public void testValidDir() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        new DocumentDirectoryFQN("/");
        new DocumentDirectoryFQN("a");
        new DocumentDirectoryFQN("");
        new DocumentDirectoryFQN("/affe/a");
    }

    @Test (expected = BaseException.class)
    public void testNonValidDir1() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        new DocumentDirectoryFQN("//");
    }

    @Test (expected = BaseException.class)
    public void testNonValidDir2() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        new DocumentDirectoryFQN("a/");
    }

    @Test (expected = BaseException.class)
    public void testNonValidDir3() {
        LOGGER.info("START TEST " + new RuntimeException("").getStackTrace()[0].getMethodName());
        new DocumentDirectoryFQN("a/b//c");
    }

}
