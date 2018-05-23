package org.adorsys.docusafe.service.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.utils.HexUtil;
import org.adorsys.encobject.service.api.EncryptionStreamService;
import org.adorsys.encobject.service.api.KeySource;
import org.adorsys.encobject.types.KeyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;

/**
 * Created by peter on 23.05.18 at 11:44.
 */
public class NoEncryptionStreamServiceImpl implements EncryptionStreamService {

    @Override
    public InputStream getEncryptedInputStream(InputStream inputStream, KeySource keySource, KeyID keyID, Boolean compress) {
        Key key = keySource.readKey(keyID);
        return new WriteKeyFirstInputStream(key, inputStream);
    }

    @Override
    public InputStream getDecryptedInputStream(InputStream inputStream, KeySource keySource, KeyID keyID) {
        Key key = keySource.readKey(keyID);
        return new ReadKeyFirstInputStream(key, inputStream);
    }

    private static class WriteKeyFirstInputStream extends InputStream {
        // private final static Logger LOGGER = LoggerFactory.getLogger(WriteKeyFirstInputStream.class);

        private final InputStream origInputStream;
        private String keyString;
        private int keyStringLength;
        private int keyStringIndex;
        private boolean keyWritten = false;

        public WriteKeyFirstInputStream(Key key, InputStream origInputStream) {
            this.keyString = HexUtil.convertBytesToHexString(key.getEncoded());
            this.keyStringLength = keyString.length();
            this.keyStringIndex = 0;
            this.origInputStream = origInputStream;
            // LOGGER.debug("create NO ENCRYPTION with key " + keyString);
        }

        @Override
        public int read() throws IOException {
            //    LOGGER.debug("read");
            if (keyWritten) {
                //        LOGGER.debug("return real read");
                return origInputStream.read();
            }
            //   LOGGER.debug("return postion " + keyStringIndex + " + of key " + new String(keyString));
            char i = keyString.charAt(keyStringIndex++);
            if (keyStringIndex == keyStringLength) {
                keyWritten = true;
            }
            //    LOGGER.debug("returend value for read is " + i);
            return i;
        }
    }

    private static class ReadKeyFirstInputStream extends InputStream {
        //    private final static Logger LOGGER = LoggerFactory.getLogger(ReadKeyFirstInputStream.class);

        private final InputStream origInputStream;
        private String keyString;
        private int keyStringLength;
        private int keyStringIndex = 0;
        private boolean keyRead = false;

        public ReadKeyFirstInputStream(Key key, InputStream origInputStream) {
            this.keyString = HexUtil.convertBytesToHexString(key.getEncoded());
            this.keyStringLength = keyString.length();
            this.keyStringIndex = 0;
            this.origInputStream = origInputStream;
            //    LOGGER.debug("create NO DECRYPTION with key " + keyString);
        }

        @Override
        public int read() throws IOException {
            //         LOGGER.debug("read");
            while (!keyRead) {
                //         LOGGER.debug("check postion " + keyStringIndex + " + of key " + keyString);
                char i1 = keyString.charAt(keyStringIndex++);
                char i2 = (char) origInputStream.read();
                if (i1 != i2) {
                    throw new BaseException("WRONG KEY. total length of key: " + keyStringLength + " wrong position:" + (keyStringIndex - 1));
                }
                if (keyStringIndex == keyStringLength) {
                    keyRead = true;
                }
            }
            //        LOGGER.debug("return real read");
            return origInputStream.read();
        }
    }
}