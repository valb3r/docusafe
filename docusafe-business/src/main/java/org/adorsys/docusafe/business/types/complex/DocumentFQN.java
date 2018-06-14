package org.adorsys.docusafe.business.types.complex;

import org.adorsys.cryptoutils.basetypes.BaseTypeString;
import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.encobject.complextypes.BucketPath;

/**
 * Created by peter on 20.01.18 at 07:29.
 * Ist für den Benutzer der absolute Pfad einer Datei. Im System ist dieser
 * Pfad aber nur relativ, da noch der UserHomeBucketPath davor gesetzt werden muss.
 * Dieser Pfad beinhaltet auch schon den Dateinamen. Dieser ist einfach das
 * letzte Element im Pfad
 */
public class DocumentFQN extends BaseTypeString {
    public DocumentFQN(String value) {
        super(prependStartingSeparator(value));
    }

    public DocumentDirectoryFQN getDocumentDirectory() {
        String value = getValue();
        int lastIndex = value.lastIndexOf(BucketPath.BUCKET_SEPARATOR);
        return new DocumentDirectoryFQN(value.substring(0, lastIndex));
    }

    private static String prependStartingSeparator(String s) {
        if (s.startsWith(BucketPath.BUCKET_SEPARATOR)) {
            return s;
        }
        return BucketPath.BUCKET_SEPARATOR + s;
    }

}
