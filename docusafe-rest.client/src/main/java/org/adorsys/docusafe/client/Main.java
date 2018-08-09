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

    public static final String USER_ID = "peter";
    public static final String PASSWORD = "rkp";

    public static void main(String[] args) {
        if (args.length == 0) {
            error();
        }
        String action = args[0];

        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        DocumentsafeRestClient client = new DocumentsafeRestClient();
        if (action.equals("-cu")) {
            if (args.length != 1) {
                error();
            }
            client.createUser(USER_ID, PASSWORD);
        }
        if (action.equals("-ws")) {
            if (args.length != 2) {
                error();
            }
            String filename = args[1];
            client.writeDocumentStream(USER_ID, PASSWORD, filename, getAsInputStream(filename), new File(filename).length());
//            client.writeDocumentStream(USER_ID, PASSWORD, filename, new SlowInputStream(getAsInputStream(filename), 1, 1024 * 1024), new File(filename).length());
        }
        if (action.equals("-rs")) {
            if (args.length != 3) {
                error();
            }
            String filename = args[1];
            String localfilename = args[2];
            client.readDocumentStream(USER_ID, PASSWORD, filename, localfilename);
        }
        if (action.equals("-wb")) {
            if (args.length != 2) {
                error();
            }
            String filename = args[1];
            client.writeDocument(USER_ID, PASSWORD, filename, getAsBytes(filename));
        }
        if (action.equals("-rb")) {
            if (args.length != 3) {
                error();
            }
            String localfilename = args[2];
            String filename = args[1];
            client.readDocument(USER_ID, PASSWORD, filename, localfilename);
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
            LOGGER.debug("ok, receive an inputstream");
            int available = 0;
            int limit = 100;
            while ((available = inputStream.available()) > 0) {
                int min = Math.min(limit, available);
                byte[] bytes = new byte[min];
                int read = inputStream.read(bytes, 0, min);
                if (read != min) {
                    throw new BaseException("expected to read " + min + " bytes, but read " + read + " bytes");
                }
                LOGGER.debug("READ " + min + " bytes:" + HexUtil.convertBytesToHexString(bytes));
            }
            LOGGER.debug("finished reading");
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private static void error() {
        LOGGER.debug("Pass params: -cu create user");
        LOGGER.debug("Pass params: -ws file (write stream)");
        LOGGER.debug("Pass params: -rs file localfile (read stream)");
        LOGGER.debug("Pass params: -wb file (write bytes)");
        LOGGER.debug("Pass params: -rb file localfilename (read bytes)");
        System.exit(1);
    }

}
