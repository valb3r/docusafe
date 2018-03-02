package org.adorsys.documentsafe.layer03business.types.complex;

import org.adorsys.cryptoutils.basetypes.BaseTypeString;
import org.adorsys.encobject.complextypes.BucketPath;

/**
 * Created by peter on 20.01.18 at 07:29.
 * Ist für den Benutzer der absolute Pfad einer Datei. Im System ist dieser
 * Pfad aber nur relativ, da noch der UserHomeBucketPath davor gesetzt werden muss.
 * Dieser Pfad beinhaltet auch schon den Dateinamen. Dieser ist einfach das
 * letzte Element im Pfad
 */
public class DocumentDirectoryFQN extends BaseTypeString {
    public DocumentDirectoryFQN(String value) {
        super(value);
    }

    public DocumentFQN addName(String value) {
        return new DocumentFQN(getValue() + BucketPath.BUCKET_SEPARATOR + value);
    }

    public DocumentDirectoryFQN addDirectory(String value) {
        return new DocumentDirectoryFQN(getValue() + BucketPath.BUCKET_SEPARATOR + value);
    }
}
