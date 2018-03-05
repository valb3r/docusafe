package org.adorsys.docusafe.client;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.utils.HexUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by peter on 27.02.18 at 09:49.
 */
public class Main {
    private final static Logger LOGGER = LoggerFactory.getLogger(Main.class);
    static final String BASEURI = "http://localhost:8080";

    public static void main(String[] args) {
        if (args.length == 0) {
            LOGGER.info("no args given");
            return;
        }
        String filename = args[0];

        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        DocumentsafeRestClient client = new DocumentsafeRestClient(BASEURI);
        client.createUser("peter", "kennwort");
        // client.readDocument("peter", "kennwort", "README.txt");
        // client.writeDocument("peter", "kennwort", "folder1/client-1.0-SNAPSHOT.jar", getAsBytes(SMALL_DOCUMENT));
        // showInputStream(getAsInputStream("documentsafe-1.0-SNAPSHOT.jar"));
        client.writeDocumentStream("peter", "kennwort", filename, new SlowInputStream(getAsInputStream(filename), 1, 1024*1024), new File(filename).length());
        // client.writeDocumentStream2("peter", "kennwort", "folder1/documentsafe-1.0-SNAPSHOT.jar", getAsInputStream(LARGE_DOCUMENT));
    }

    public static InputStream getAsInputStream(String filename) {
        try {
            return new FileInputStream(new File(filename));
        } catch (Exception e) {
            LOGGER.error("file:" + new File(filename).getAbsoluteFile());
            throw BaseExceptionHandler.handle(e);
        }
    }

    public static byte[] getAsBytes(String filename) {
        try {
            return FileUtils.readFileToByteArray(new File(filename));
        } catch (Exception e) {
            LOGGER.error("file:" + new File(filename).getAbsoluteFile());
            throw BaseExceptionHandler.handle(e);
        }
    }

    public static void showInputStream(InputStream inputStream) {
        try {
            LOGGER.info("ok, receive an inputstream");
            int available = 0;
            int limit = 100;
            while ((available = inputStream.available()) > 0) {
                int min = Math.min(limit, available);
                byte[] bytes = new byte[min];
                int read = inputStream.read(bytes, 0, min);
                if (read != min) {
                    throw new BaseException("expected to read " + min + " bytes, but read " + read + " bytes");
                }
                LOGGER.info("READ " + min + " bytes:" + HexUtil.convertBytesToHexString(bytes));
            }
            LOGGER.info("finished reading");
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
