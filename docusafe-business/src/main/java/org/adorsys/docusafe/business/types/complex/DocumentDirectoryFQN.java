package org.adorsys.docusafe.business.types.complex;

import org.adorsys.cryptoutils.basetypes.BaseTypeString;
import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;

/**
 * Created by peter on 20.01.18 at 07:29.
 * Ist für den Benutzer der absolute Pfad einer Datei. Im System ist dieser
 * Pfad aber nur relativ, da noch der UserHomeBucketPath davor gesetzt werden muss.
 * Dieser Pfad beinhaltet auch schon den Dateinamen. Dieser ist einfach das
 * letzte Element im Pfad
 *
 * WICHTIG
 * Jeder DocumenteDirectoryFQN beginnt immer mit einem Slash, d.h. die Länge ist immer minimal 1
 */
public class DocumentDirectoryFQN extends BaseTypeString {
    public DocumentDirectoryFQN(String value) {
        super(prependStartingSeparator(value));
    }

    public DocumentFQN addName(String value) {
        if (getValue().length() == 1) {
            return new DocumentFQN(BucketPath.BUCKET_SEPARATOR + value);
        }
        return new DocumentFQN(getValue() + BucketPath.BUCKET_SEPARATOR + value);
    }

    public DocumentDirectoryFQN addDirectory(String value) {
        if (getValue().length() == 1) {
            return new DocumentDirectoryFQN(BucketPath.BUCKET_SEPARATOR + value);
        }
        return new DocumentDirectoryFQN(getValue() + BucketPath.BUCKET_SEPARATOR + value);
    }

    public BucketDirectory prepend(BucketDirectory bucketDirectory) {
        if (getValue().length() == 1) {
            return bucketDirectory;
        }
        return bucketDirectory.appendDirectory(getValue());
    }

    private static String prependStartingSeparator(String s) {
        if (s.startsWith(BucketPath.BUCKET_SEPARATOR)) {
            if (s.length() > 1 && s.startsWith("//")) {
                throw new BaseException("programming error, path never should start with two dashes" + s);
            }
            return s;
        }
        return BucketPath.BUCKET_SEPARATOR + s;
    }
}
