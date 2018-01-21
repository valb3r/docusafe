package org.adorsys.documentsafe.layer01persistence.types.complextypes;

import org.adorsys.documentsafe.layer01persistence.types.BucketName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

/**
 * Created by peter on 16.01.18.
 */
public class BucketPath {
    private final static Logger LOGGER = LoggerFactory.getLogger(BucketPath.class);

    List<BucketName> buckets = new ArrayList<>();

    public BucketPath() {
    }

    public BucketPath(String path) {
        buckets = split(path);
    }

    public BucketPath(BucketPath bucketPath) {
        buckets.addAll(bucketPath.buckets);
    }

    public BucketPath append(BucketPath bucketPath) {
        BucketPath appendedBucketPath = new BucketPath(this);
        appendedBucketPath.buckets.addAll(bucketPath.buckets);
        return appendedBucketPath;
    }

    public BucketPath set(BucketName bucketName) {
        buckets.clear();
        return sub(bucketName);
    }

    public BucketPath sub(BucketName bucketName) {
        buckets.add(bucketName);
        return this;
    }

    public String getObjectHandlePath() {
        return buckets.stream().map(b -> b.getValue()).collect(Collectors.joining(BucketName.BUCKET_SEPARATOR));
    }

    public BucketName getFirstBucket() {
        return buckets.get(0);
    }

    public String getSubBuckets() {
        if (getDepth() <= 1) {
            return "";
        }
        List<BucketName> remaining = new ArrayList<>();
        remaining.addAll(buckets);
        remaining.remove(0);
        String path = remaining.stream().map(b -> b.getValue()).collect(Collectors.joining(BucketName.BUCKET_SEPARATOR));
        path = path + "/";

        return path;
    }

    public int getDepth() {
        return buckets.size();
    }

    private static List<BucketName> split(String fullBucketPath) {
        List<BucketName> list = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(fullBucketPath, BucketName.BUCKET_SEPARATOR);
        while (st.hasMoreElements()) {
            list.add(new BucketName(st.nextToken()));
        }
        return list;
    }

    @Override
    public String toString() {
        return "BucketPath{" +
                "buckets=" + buckets +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BucketPath that = (BucketPath) o;

        return buckets.equals(that.buckets);

    }

    @Override
    public int hashCode() {
        return buckets.hashCode();
    }
}
