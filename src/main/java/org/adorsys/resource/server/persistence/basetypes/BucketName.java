package org.adorsys.resource.server.persistence.basetypes;

/**
 * Created by peter on 23.12.2017 at 18:03:52.
 */
public class BucketName extends BaseTypeString {

	/**
	 * Separator between guard name and containing bucket.
	 */
	public static final String BUCKET_SEPARATOR = "@";

	public BucketName() {}

    public BucketName(String value) {
        super(value);
    }
}
