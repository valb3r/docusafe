package org.adorsys.docusafe.client;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by peter on 28.02.18 at 15:24.
 */
public class SlowInputStreamTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(SlowInputStream.class);
    @Test
    public void test1() {
        try {
            int size = 6;
            byte[] bytes = new byte[size];
            for (int i = 0; i < size; i++) {
                bytes[i] = (byte) i;
            }
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

            InputStream sis = new SlowInputStream(bis, 1, 2);

            int value = 0;
            int counter = 0;
            while ((value = sis.read()) != -1) {
                counter++;
            }
            LOGGER.debug("read " + counter + " bytes");
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }
}
