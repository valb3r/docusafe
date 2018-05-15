package org.adorsys.docusafe.client;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Created by peter on 28.02.18 at 14:29.
 * Dieser Strom liefert X bytes alle Y Sekunden.
 * Wenn weniger als X bytes gelesen werden, dann können diese beliebig schnell nachgelesen werden.
 * Wenn aber X bytes gelesen wurden, dann muss wieder Y Sekunden gewartet werden, bevor überhaupt
 * erst ein Byte zur Verfügung steht. Wird aber z.B. erst nach 3*Y Sekunden wieder gelesen, dann
 * stehen auch 3*X Bytes zur Verfügung.
 *
 */
public class SlowInputStream extends InputStream {
    private final static Logger LOGGER = LoggerFactory.getLogger(SlowInputStream.class);
    private InputStream source;
    private Date firstRead = new Date();
    private int secsForOneChunk;
    private int chunkSize;
    private long readSoFar = 0;

    /**
     *
     * @param source originaler InputStream
     * @param secsForOneChunk Interval von Sekunden, in dem weitere Chunks zur Verfügung gestellt werden
     * @param chunkSize maximale Anzahl der Bytes eines Chunks
     */
    public SlowInputStream(InputStream source, int secsForOneChunk, int chunkSize) {
        this.chunkSize =chunkSize;
        this.secsForOneChunk = secsForOneChunk;
        this.source = source;
        if (chunkSize <= 1) {
            throw new BaseException("chunk size has to be bigger than one");
        }
        if (secsForOneChunk < 1) {
            throw new BaseException("number of secs to wait has to be bigger than one");
        }
    }

    /**
     * @return liefert genau ein byte zurück. Oder -1, wenn der Stream zu Ende ist.
     * @throws IOException
     */
    public int read() throws IOException {
        // LOGGER.debug("start read 1 byte");
        int value = source.read();
        readSoFar++;

        if (readSoFar % chunkSize == 1) {
            LOGGER.debug("returned so far " + readSoFar);
            int diff = getDiff();
            int numberOfChuncsAllowedToReadSoFar = diff / secsForOneChunk;
            // long bytesAllowedToReadSoFar = numberOfChuncsAllowedToReadSoFar * chunkSize;
            // LOGGER.debug("Chunksize wurde zuvor erreicht.  Bisher hätten gelesen werden dürfen:" + bytesAllowedToReadSoFar + " es wurden gelesen:" + readSoFar);
            if (readSoFar > numberOfChuncsAllowedToReadSoFar) {
                sleep(((numberOfChuncsAllowedToReadSoFar + 1) * secsForOneChunk) - diff);
            }
        }
        // LOGGER.debug("end read 1 byte");
        return value;
    }

    private int getDiff() {
        Date now = new Date();
        long diff = now.getTime() - firstRead.getTime();
        diff = diff / 1000;
        LOGGER.debug("Zeit von " + firstRead + " bis " + now + " sind " + diff + " secs.");
        return (int) diff;
    }

    private void sleep(int secs) {
        LOGGER.debug("sleep for " + secs + " secs");
        try {
            Thread.currentThread().sleep(secs * 1000);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
        LOGGER.debug("finished sleep for " + secs + " secs");
    }

}
