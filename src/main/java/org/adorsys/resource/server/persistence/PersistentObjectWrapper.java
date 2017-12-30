package org.adorsys.resource.server.persistence;

import org.adorsys.encobject.domain.ContentMetaInfo;
import org.adorsys.encobject.domain.ObjectHandle;

public class PersistentObjectWrapper {
	private byte[] data;
	private ContentMetaInfo metaIno;
	private String keyID;
	private ObjectHandle handle;

	public PersistentObjectWrapper(byte[] data, ContentMetaInfo metaIno, String keyID, ObjectHandle handle) {
		super();
		this.data = data;
		this.metaIno = metaIno;
		this.keyID = keyID;
		this.handle = handle;
	}
	
	public ObjectHandle getHandle() {
		return handle;
	}
	public byte[] getData() {
		return data;
	}
	public ContentMetaInfo getMetaIno() {
		return metaIno;
	}
	public String getKeyID() {
		return keyID;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public void setMetaIno(ContentMetaInfo metaIno) {
		this.metaIno = metaIno;
	}

	public void setKeyID(String keyID) {
		this.keyID = keyID;
	}

	public void setHandle(ObjectHandle handle) {
		this.handle = handle;
	}
	
}
