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
        if (args.length < 2) {
            LOGGER.info("Pass params: -ws file (write stream)");
            LOGGER.info("Pass params: -wb file (write bytes)");
            LOGGER.info("Pass params: -rb file localfilename (read bytes)");
            return;
        }
        String action = args[0];
        String filename = args[1];



        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        DocumentsafeRestClient client = new DocumentsafeRestClient(BASEURI);
        client.createUser("peter", "kennwort");
        if (action.equals("-wb")) {
            client.writeDocument("peter", "kennwort", filename, getAsBytes(filename));
        }
        if (action.equals("-rb")) {
            String localfilename = args[2];
            client.readDocument("peter", "kennwort", filename, localfilename);
        }
        if (action.equals("-ws")) {
            client.writeDocumentStream("peter", "kennwort", filename, new SlowInputStream(getAsInputStream(filename), 1, 1024*1024), new File(filename).length());
        }
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
