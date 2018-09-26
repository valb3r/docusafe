package org.adorsys.docusafe.business.impl.caches;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.utils.HexUtil;
import org.adorsys.docusafe.business.exceptions.PathDecryptionException;
import org.adorsys.docusafe.business.exceptions.PathEncryptionException;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.complextypes.BucketPathUtil;
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
    public final static boolean encryptContainer = false;  // Darf nur dann auf true gesetzt werden, wenn es ein universales Kennwort gibt.
    // Andernfalls könnte userExists nicht funktionieren

    public static BucketDirectory encrypt(UserIDAuth userIDAuth, BucketDirectory bucketDirectory) {
        return new BucketDirectory(encrypt(userIDAuth, BucketPathUtil.getAsString(bucketDirectory)));
    }

    public static BucketPath encrypt(UserIDAuth userIDAuth, BucketPath bucketPath) {
        return new BucketPath(encrypt(userIDAuth, BucketPathUtil.getAsString(bucketPath)));
    }

    public static BucketDirectory decrypt(UserIDAuth userIDAuth, BucketDirectory bucketDirectory) {
        return new BucketDirectory(decrypt(userIDAuth, BucketPathUtil.getAsString(bucketDirectory)));
    }

    public static BucketPath decrypt(UserIDAuth userIDAuth, BucketPath bucketPath) {
        return new BucketPath(decrypt(userIDAuth, BucketPathUtil.getAsString(bucketPath)));
    }

    private static String encrypt(UserIDAuth userIDAuth, String fullString) {
        try {
            Cipher cipher = createCipher(userIDAuth, Cipher.ENCRYPT_MODE);

            StringBuilder encryptedPath = new StringBuilder();
            StringTokenizer st = new StringTokenizer(fullString, BucketPath.BUCKET_SEPARATOR);
            if (! encryptContainer) {
                encryptedPath.append(BucketPath.BUCKET_SEPARATOR + st.nextToken());
            }
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

    public static String decrypt(UserIDAuth UserIDAuth, String encryptedHexString) {
        try {
            Cipher cipher = createCipher(UserIDAuth, Cipher.DECRYPT_MODE);

            StringBuilder plainPath = new StringBuilder();
            StringTokenizer st = new StringTokenizer(encryptedHexString, BucketPath.BUCKET_SEPARATOR);
            if (! encryptContainer) {
                plainPath.append(BucketPath.BUCKET_SEPARATOR + st.nextToken());
            }
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

    private static Cipher createCipher(UserIDAuth userIDAuth, int cipherMode) {
        try {
            byte[] key = (userIDAuth.getUserID().getValue() + userIDAuth.getReadKeyPassword().getValue()).getBytes("UTF-8");
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
