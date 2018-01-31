package org.adorsys.documentsafe.layer03business.types.complex;

import org.adorsys.cryptoutils.basetypes.BaseTypeString;

/**
 * Created by peter on 20.01.18 at 07:29.
 * Ist für den Benutzer der absolute Pfad einer Datei. Im System ist dieser
 * Pfad aber nur relativ, da noch der UserHomeBucketPath davor gesetzt werden muss.
 * Dieser Pfad beinhaltet auch schon den Dateinamen. Dieser ist einfach das
 * letzte Element im Pfad
 */
public class DocumentFQN extends BaseTypeString {
    public DocumentFQN(String value) {
        super(value);
    }
}
