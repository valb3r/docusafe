package org.adorsys.documentsafe.layer02service.impl;

import org.adorsys.documentsafe.layer00common.exceptions.BaseException;
import org.adorsys.documentsafe.layer01persistence.types.complextypes.BucketPath;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.domain.Tier;
import org.jclouds.domain.Location;
import org.jclouds.domain.ResourceMetadata;

import java.net.URI;
import java.util.Date;
import java.util.Map;

/**
 * Created by peter on 20.01.18 at 16:38.
 */
public class ExtendedStorageMetadata implements StorageMetadata {
    private StorageMetadata meta;
    private BucketPath bucketPath;

    public ExtendedStorageMetadata(BucketPath bucketPath, StorageMetadata meta) {
        this.meta = meta;
        this.bucketPath = bucketPath;
    }

    // hier wird der Pfad abgezogen
    @Override
    public String getName() {
        String fullname = meta.getName();
        String subBuckets = bucketPath.getSubBuckets();
        if (!fullname.startsWith(subBuckets)) {
            throw new BaseException("Expected path starting with " + subBuckets + " but was " + fullname);
        }
        return fullname.substring(subBuckets.length());
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
    public Tier getTier() {
        return meta.getTier();
    }

    @Override
    public int compareTo(ResourceMetadata<StorageType> o) {
        return meta.compareTo(o);
    }
}
