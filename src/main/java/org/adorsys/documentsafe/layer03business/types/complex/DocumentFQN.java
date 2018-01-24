package org.adorsys.documentsafe.layer03business.types.complex;

import org.adorsys.documentsafe.layer00common.basetypes.BaseTypeString;
import org.adorsys.documentsafe.layer00common.exceptions.BaseException;
import org.adorsys.documentsafe.layer01persistence.types.BucketName;
import org.adorsys.documentsafe.layer02service.types.DocumentID;
import org.adorsys.documentsafe.layer03business.types.RelativeBucketPath;

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
    private final RelativeBucketPath relativeBucketPath;
    private final DocumentID documentID;
    
    public DocumentFQN(String value) {
        super(value);
        {   // extract relative BucketPath
            List<String> list = split(value);
            if (list.isEmpty()) {
                throw new BaseException("FQN must not be empty: " + value);
            }
            if (list.size() == 1) {
                relativeBucketPath = new RelativeBucketPath("");
            } else {
                relativeBucketPath = new RelativeBucketPath(list.get(0));
                for (int i = 1; i < list.size() - 1; i++) {
                    relativeBucketPath.sub(new BucketName(list.get(i)));
                }
            }
        }
        {   // extract documentID
            List<String> list = split(value);
            if (list.isEmpty()) {
                throw new BaseException("FQN must not be empty: " + value);
            }
            documentID = new DocumentID(list.get(list.size() - 1));
        }
    }

    public RelativeBucketPath getRelativeBucketPath() {
        return relativeBucketPath;
    }

    public DocumentID getDocumentID() {
        return documentID;
    }

    private static List<String> split(String FQN) {
        List<String> list = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(FQN, BucketName.BUCKET_SEPARATOR);
        while (st.hasMoreElements()) {
            list.add(st.nextToken());
        }
        return list;
    }

    @Override
    public String toString() {
        return "DocumentFQN{" +
                "relativeBucketPath=" + relativeBucketPath +
                ", documentID=" + documentID +
                '}';
    }
}
