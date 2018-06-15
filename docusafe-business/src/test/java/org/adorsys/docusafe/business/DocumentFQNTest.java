package org.adorsys.docusafe.business;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.junit.Test;

/**
 * Created by peter on 15.06.18 at 14:17.
 */
public class DocumentFQNTest {

    @Test
    public void testValidName() {
        new DocumentFQN("/");
        new DocumentFQN("a");
        new DocumentFQN("");
        new DocumentFQN("/affe/a");
    }


    @Test (expected = BaseException.class)
    public void testNonValid1() {
        new DocumentFQN("//");
    }

    @Test (expected = BaseException.class)
    public void testNonValid2() {
        new DocumentFQN("a/");
    }

    @Test (expected = BaseException.class)
    public void testNonValid3() {
        new DocumentFQN("a/b//c");
    }


    @Test
    public void testValidDir() {
        new DocumentDirectoryFQN("/");
        new DocumentDirectoryFQN("a");
        new DocumentDirectoryFQN("");
        new DocumentDirectoryFQN("/affe/a");
    }

    @Test (expected = BaseException.class)
    public void testNonValidDir1() {
        new DocumentDirectoryFQN("//");
    }

    @Test (expected = BaseException.class)
    public void testNonValidDir2() {
        new DocumentDirectoryFQN("a/");
    }

    @Test (expected = BaseException.class)
    public void testNonValidDir3() {
        new DocumentDirectoryFQN("a/b//c");
    }

}
