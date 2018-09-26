package org.adorsys.docusafe.business.impl.caches;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.utils.HexUtil;
import org.adorsys.docusafe.business.exceptions.PathDecryptionException;
import org.adorsys.docusafe.business.exceptions.PathEncryptionException;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.complextypes.BucketPathUtil;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * Created by peter on 25.09.18.
 */
public class BucketPathEncryption {
    private final static Logger LOGGER = LoggerFactory.getLogger(BucketPathEncryption.class);
    private final static Charset CHARSET = Charset.forName("UTF-8");

    public static BucketDirectory encrypt(ReadKeyPassword readKeyPassword, BucketDirectory bucketDirectory) {
        return new BucketDirectory(encrypt(readKeyPassword, BucketPathUtil.getAsString(bucketDirectory)));
    }

    public static BucketPath encrypt(ReadKeyPassword readKeyPassword, BucketPath bucketPath) {
        return new BucketPath(encrypt(readKeyPassword, BucketPathUtil.getAsString(bucketPath)));
    }

    public static BucketDirectory decrypt(ReadKeyPassword readKeyPassword, BucketDirectory bucketDirectory) {
        return new BucketDirectory(decrypt(readKeyPassword, BucketPathUtil.getAsString(bucketDirectory)));
    }

    public static BucketPath decrypt(ReadKeyPassword readKeyPassword, BucketPath bucketPath) {
        return new BucketPath(decrypt(readKeyPassword, BucketPathUtil.getAsString(bucketPath)));
    }

    private static String encrypt(ReadKeyPassword readKeyPassword, String fullString) {
        try {
            Cipher cipher = createCipher(readKeyPassword, Cipher.ENCRYPT_MODE);

            StringBuilder encryptedPath = new StringBuilder();
            StringTokenizer st = new StringTokenizer(fullString, BucketPath.BUCKET_SEPARATOR);
            while (st.hasMoreTokens()) {

                String plainString = st.nextToken();
                // LOGGER.debug("encrypt: plain string " + plainString);
                byte[] plainBytes = plainString.getBytes(CHARSET);
                // LOGGER.debug("encrypt: plain bytes as hex string " + HexUtil.convertBytesToHexString(plainBytes));
                byte[] encryptedBytes = cipher.doFinal(plainBytes);
                String encryptedBytesAsHexString = HexUtil.convertBytesToHexString(encryptedBytes).toLowerCase();
                // LOGGER.debug("encrypt: ecnrypted bytes as hex string " + encryptedBytesAsHexString);
                encryptedPath.append(BucketPath.BUCKET_SEPARATOR + encryptedBytesAsHexString);
            }
            return encryptedPath.toString();
        } catch (Exception e) {
            throw new PathEncryptionException(fullString, e);
        }
    }

    public static String decrypt(ReadKeyPassword readKeyPassword, String encryptedHexString) {
        try {
            Cipher cipher = createCipher(readKeyPassword, Cipher.DECRYPT_MODE);

            StringBuilder plainPath = new StringBuilder();
            StringTokenizer st = new StringTokenizer(encryptedHexString, BucketPath.BUCKET_SEPARATOR);
            while (st.hasMoreTokens()) {
                String encryptedBytesAsHexString = st.nextToken();
                // LOGGER.debug("decrypt: encrpyted bytes as hex string:" + encryptedBytesAsHexString);
                byte[] encryptedBytes = HexUtil.convertHexStringToBytes(encryptedBytesAsHexString.toUpperCase());
                // LOGGER.debug("decrypt: encrpyted bytes as hex string:" + HexUtil.convertBytesToHexString(encryptedBytes));
                byte[] plainBytes = cipher.doFinal(encryptedBytes);
                // LOGGER.debug("decrypt: plain bytes as hex string:" + HexUtil.convertBytesToHexString(plainBytes));
                String plainString = new String(plainBytes, CHARSET);
                // LOGGER.debug("decrypt: plain string " + plainString);

                plainPath.append(BucketPath.BUCKET_SEPARATOR + plainString);
            }
            return plainPath.toString();
        } catch (Exception e) {
            throw new PathDecryptionException(encryptedHexString, e);
        }
    }

    private static Cipher createCipher(ReadKeyPassword readKeyPassword, int cipherMode) {
        try {
            byte[] key = (readKeyPassword.getValue()).getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            // nur die ersten 128 bit nutzen
            key = Arrays.copyOf(key, 16);
            // der fertige Schluessel
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(cipherMode, secretKeySpec);
            return cipher;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }
}
