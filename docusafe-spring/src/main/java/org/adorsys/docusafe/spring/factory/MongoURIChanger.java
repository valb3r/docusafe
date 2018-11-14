package org.adorsys.docusafe.spring.factory;

import org.adorsys.encobject.types.connection.MongoURI;

/**
 * Created by peter on 14.11.18 19:10.
 */
public class MongoURIChanger {
    private MongoURI mongoURI;
    public MongoURIChanger(MongoURI mongoURI) {
        this.mongoURI = mongoURI;
    }

    public MongoURI modifyRootBucket(String rootBucket) {
        String uri = mongoURI.getValue();
        int beginIndex = uri.lastIndexOf("/") + 1;
        int endIndex1 = uri.indexOf("?");
        int endIndex2 = uri.indexOf("&");
        int endIndex = (endIndex1 != -1 && endIndex2 != -1) ? Math.min(endIndex1, endIndex2) : Math.max(endIndex1, endIndex2);

        String part1 = uri.substring(0,beginIndex);
        String part2 = null;
        if (endIndex == -1) {
            part2 = "";
        } else {
            part2 = uri.substring(endIndex);
        }
        String newUri = part1 + rootBucket + part2;
        return new MongoURI(newUri);
    }
}
