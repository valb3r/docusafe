package org.adorsys.documentsafe.layer03business.types;

import org.adorsys.documentsafe.layer00common.basetypes.BaseTypeString;
import org.adorsys.documentsafe.layer00common.exceptions.BaseException;
import org.adorsys.documentsafe.layer01persistence.types.BucketName;
import org.adorsys.documentsafe.layer02service.types.DocumentID;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by peter on 20.01.18 at 07:29.
 * Ist für den Benutzer der absolute Pfad einer Datei. Im System ist dieser
 * Pfad aber nur relativ, da noch der UserHomeBucketPath davor gesetzt werden muss.
 * Dieser Pfad beinhaltet auch schon den Dateinamen. Dieser ist einfach das
 * letzte Element im Pfad
 * <p>
 * DocumentFQN = RelativeBucketPath + DocumentID
 */
public class DocumentFQN extends BaseTypeString {
    public DocumentFQN(String value) {
        super(value);
    }

    public RelativeBucketPath getRelativeBucketPath() {
        List<String> list = split(getValue());
        if (list.isEmpty()) {
            throw new BaseException("FQN must not be empty: " + getValue());
        }
        if (list.size() == 1) {
            return new RelativeBucketPath("");
        }
        RelativeBucketPath relativeBucketPath = new RelativeBucketPath(list.get(0));
        for (int i = 1; i < list.size() - 1; i++) {
            relativeBucketPath.sub(new BucketName(list.get(i)));
        }
        return relativeBucketPath;
    }

    public DocumentID getDocumentID() {
        List<String> list = split(getValue());
        if (list.isEmpty()) {
            throw new BaseException("FQN must not be empty: " + getValue());
        }
        return new DocumentID(list.get(list.size() - 1));
    }

    private static List<String> split(String FQN) {
        List<String> list = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(FQN, BucketName.BUCKET_SEPARATOR);
        while (st.hasMoreElements()) {
            list.add(st.nextToken());
        }
        return list;
    }

}
