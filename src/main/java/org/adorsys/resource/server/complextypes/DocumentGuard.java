package org.adorsys.resource.server.complextypes;

import org.adorsys.resource.server.basetypes.DocumentGuardName;
import org.adorsys.resource.server.basetypes.DocumnentKeyID;
import org.adorsys.resource.server.basetypes.EncryptedDocumentKey;
import org.adorsys.resource.server.basetypes.GuardKeyID;

/**
 * Created by peter on 23.12.17 at 18:33.
 */
public class DocumentGuard {
    DocumentGuardName documentGuardName;
    DocumnentKeyID documnentKeyID;
    GuardKeyID guardKeyID;
    EncryptedDocumentKey encryptedDocumentKey;

    private byte[] bytes;
    public DocumentGuard(byte[] content) {
        bytes = content;
        // read bytes to get not encrypted Header and the EncryptedDocumentKey
    }

    public DocumentGuardName getDocumentGuardName() {
        return documentGuardName;
    }

    public DocumnentKeyID getDocumnentKeyID() {
        return documnentKeyID;
    }

    public EncryptedDocumentKey getEncryptedDocumentKey() {
        return encryptedDocumentKey;
    }

    public GuardKeyID getGuardKeyID() {
        return guardKeyID;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
