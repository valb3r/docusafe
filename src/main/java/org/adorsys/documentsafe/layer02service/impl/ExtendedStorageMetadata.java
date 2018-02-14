package org.adorsys.documentsafe.layer02service.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.encobject.complextypes.BucketDirectory;
import org.adorsys.encobject.complextypes.BucketPath;
import org.adorsys.encobject.domain.Location;
import org.adorsys.encobject.domain.ResourceMetadata;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.domain.StorageType;

import java.net.URI;
import java.util.Date;
import java.util.Map;

/**
 * Created by peter on 20.01.18 at 16:38.
 */
public class ExtendedStorageMetadata implements StorageMetadata {
    private StorageMetadata meta;
    private BucketDirectory bucketDirectory;

    public ExtendedStorageMetadata(BucketDirectory bucketDirectory, StorageMetadata meta) {
        this.meta = meta;
        this.bucketDirectory = bucketDirectory;
    }

    // hier wird der Pfad abgezogen
    @Override
    public String getName() {
        String fullname = meta.getName();
        if (bucketDirectory.getObjectHandle().getName() != null) {
            String subBuckets = bucketDirectory.getObjectHandle().getName() + BucketPath.BUCKET_SEPARATOR;
            if (!fullname.startsWith(subBuckets)) {
                throw new BaseException("Expected path starting with " + subBuckets + " but was " + fullname);
            }
            return fullname.substring(subBuckets.length());
        }
        return fullname;
    }

// alle weiteren nur Durchreichen
    @Override
    public StorageType getType() {
        return meta.getType();
    }

    @Override
    public String getProviderId() {
        return meta.getProviderId();
    }

    @Override
    public Location getLocation() {
        return meta.getLocation();
    }

    @Override
    public URI getUri() {
        return meta.getUri();
    }

    @Override
    public Map<String, String> getUserMetadata() {
        return meta.getUserMetadata();
    }

    @Override
    public String getETag() {
        return meta.getETag();
    }

    @Override
    public Date getCreationDate() {
        return meta.getCreationDate();
    }

    @Override
    public Date getLastModified() {
        return meta.getLastModified();
    }

    @Override
    public Long getSize() {
        return meta.getSize();
    }

    @Override
    public int compareTo(ResourceMetadata<StorageType> o) {
        return meta.compareTo(o);
    }
}
