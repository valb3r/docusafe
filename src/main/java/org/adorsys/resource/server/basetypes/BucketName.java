package org.adorsys.resource.server.basetypes;

import org.adorsys.resource.server.basetypes.adapter.BucketNameRestAdapter;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by peter on 23.12.2017 at 18:03:52.
 */
@XmlJavaTypeAdapter(BucketNameRestAdapter.class)
@XmlType
public class BucketName extends BaseTypeString {
    public BucketName() {}

    public BucketName(String value) {
        super(value);
    }
}
