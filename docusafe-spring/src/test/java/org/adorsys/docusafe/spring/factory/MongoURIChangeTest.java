package org.adorsys.docusafe.spring.factory;

import org.adorsys.encobject.types.connection.MongoURI;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by peter on 14.11.18 19:10.
 */
public class MongoURIChangeTest {
    @Test
    public void test1() {
        doTest("mongodb://192.168.178.60:27017/adorsys", "AFFE", "mongodb://192.168.178.60:27017/AFFE");
    }

    @Test
    public void test2() {
        doTest("mongodb://192.168.178.60:27017/adorsys&affe", "AFFE", "mongodb://192.168.178.60:27017/AFFE&affe");
    }

    @Test
    public void test3() {
        doTest("mongodb://192.168.178.60:27017/adorsys?affe", "AFFE", "mongodb://192.168.178.60:27017/AFFE?affe");
    }

    @Test
    public void test4() {
        doTest("mongodb://192.168.178.60:27017/adorsys&haus?affe", "AFFE", "mongodb://192.168.178.60:27017/AFFE&haus?affe");
    }

    private void doTest(String before, String token, String after) {
        Assert.assertEquals(new MongoURI(after), new MongoURIChanger(new MongoURI(before)).modifyRootBucket(token));
    }
}
